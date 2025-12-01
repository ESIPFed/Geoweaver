package com.gw.local;

import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
import com.gw.utils.ProcessStatusCache;
import java.io.BufferedReader;
import javax.websocket.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

/**
 * Service class for managing local session output and interaction with WebSocket. Implements the
 * Runnable interface to run in a separate thread.
 */
@Service
@Scope("prototype")
public class LocalSessionOutput implements Runnable {

  @Autowired BaseTool bt;

  @Autowired HistoryTool ht;

  @Autowired HistoryRepository historyRepository;
  
  @Autowired ProcessStatusCache processStatusCache;

  protected Logger log = LoggerFactory.getLogger(getClass());

  protected BufferedReader in;

  protected WebSocketSession out; // log&shell WebSocket (not used anymore)

  protected Session wsout;

  protected String token; // Session token

  protected boolean run = true;

  protected String history_id;

  protected String lang;

  protected String jupyterfilepath;

  protected Process theprocess;

  /** Default constructor for Spring. */
  public LocalSessionOutput() {
    // This constructor is used for Spring.
  }

  /**
   * Initializes the LocalSessionOutput with necessary parameters for running.
   *
   * @param in         BufferedReader for reading the session's output.
   * @param token      The session token.
   * @param history_id The history ID associated with the session.
   * @param lang       The programming language used in the session.
   */
  public void init(
      BufferedReader in, String token, String history_id, String lang, String jupyterfilepath) {
    log.info("LocalSessionOutput created for token " + (token != null ? token : "<null>"));
    this.in = in;
    this.token = token;
    this.run = true;
    this.history_id = history_id;
    this.lang = lang;
    this.jupyterfilepath = jupyterfilepath;
    
    // Check if we're in CLI mode (token might be null)
    if (token == null) {
      log.info("Detected CLI mode execution - WebSocket functionality will be limited");
      // In CLI mode, we'll still process the output but won't try to send it to WebSockets
      useWebSocketFallback = false;
    }
  }

  /** Stops the local session output processing. */
  public void stop() {
    run = false;
  }

  // Flag to track if we should use long polling fallback
  private boolean useWebSocketFallback = false;
  
  /**
   * Sends a message to the associated WebSocket session.
   * If WebSocket is unavailable or closed, attempts to reconnect before sending.
   * Falls back to HTTP long polling if WebSocket connection cannot be established.
   * In CLI mode, logs the message instead of attempting to send via WebSocket.
   *
   * @param msg The message to be sent to the WebSocket.
   */
  public void sendMessage2WebSocket(String msg) {
    // Check if we're in CLI mode (token is null)
    if (token == null) {
      // In CLI mode, just log the message instead of trying to send it
      log.info("CLI Mode - Output: " + msg);
      return;
    }
    
    // Check if WebSocket is null or closed and try to reconnect
    if (BaseTool.isNull(wsout) || !wsout.isOpen()) {
      log.debug("WebSocket connection is null or closed, attempting to reconnect for token: " + this.token);
      // Try to get a new session
      wsout = CommandServlet.findSessionById(token);
      
      // If we still don't have a valid session, we'll rely on the fallback mechanism
      if (BaseTool.isNull(wsout) || !wsout.isOpen()) {
        log.debug("Could not reconnect WebSocket for token: " + this.token);
      } else {
        log.debug("Successfully reconnected WebSocket for token: " + this.token);
      }
    }
    
    // Use the CommandServlet's unified message sending method
    // This handles both WebSocket and long polling automatically
    Session session = CommandServlet.sendMessageToSocket(this.token, msg);
    
    // Update our local reference if a valid session was returned
    if (session != null) {
      this.wsout = session;
      useWebSocketFallback = false;
      log.debug("Message sent via WebSocket to history_id: " + this.history_id);
    } else {
      // If no session was returned, we're likely using long polling
      useWebSocketFallback = true;
      log.debug("Message sent via long polling fallback to history_id: " + this.history_id);
    }
  }

  /**
   * Refreshes the log monitor for WebSocket interaction. If the WebSocket session is null or
   * closed, it attempts to retrieve the session and ensure it's properly registered.
   */
  public void refreshLogMonitor() {
    // if (BaseTool.isNull(wsout) || !wsout.isOpen()) {
    //   log.debug("Refreshing WebSocket connection for token: " + this.token);
    //   // Try to get a new session
    //   wsout = CommandServlet.findSessionById(token);
      
    //   // If we still don't have a valid session, log the issue
    //   if (BaseTool.isNull(wsout) || !wsout.isOpen()) {
    //     log.debug("Could not refresh WebSocket connection for token: " + this.token);
    //   } else {
    //     log.debug("Successfully refreshed WebSocket connection for token: " + this.token);
    //   }
    // }
  }

  /** Cleans the WebSocket session by removing it from the CommandServlet. */
  public void cleanLogMonitor() {
    CommandServlet.removeSessionById(history_id);
  }

  /**
   * Sets the process associated with this output session.
   * In CLI mode, the process might be null, so we need to handle that case.
   *
   * @param p The process to be associated with this session output.
   */
  public void setProcess(Process p) {
    this.theprocess = p;
    if (p == null) {
      log.warn("Null process provided to LocalSessionOutput. This may indicate a problem in CLI mode.");
    }
  }

  /**
   * Ends the process with an exit code and updates the history accordingly.
   *
   * @param token The session token.
   * @param exitvalue The exit code of the process.
   */
  public void endWithCode(String token, int exitvalue) {
    this.stop();

    // Get the latest history
    History h = ht.getHistoryById(this.history_id);
    
    if (h == null) {
      log.error("History record not found for history_id: " + this.history_id + ", cannot set exit code");
      return;
    }

    String status;
    if (exitvalue == 0) {
      status = ExecutionStatus.DONE;
      log.info("Process completed successfully (exit code: 0) for history ID: " + this.history_id);
    } else {
      status = ExecutionStatus.FAILED;
      log.warn("Process failed (exit code: " + exitvalue + ") for history ID: " + this.history_id);
    }

    h.setIndicator(status);
    h.setHistory_end_time(BaseTool.getCurrentSQLDate());
    ht.saveHistory(h);
    
    // Update cache with the status
    try {
      if (processStatusCache != null) {
        processStatusCache.updateStatus(history_id, status);
        log.debug("Updated cache for history ID: " + history_id + " with status: " + status);
      }
    } catch (Exception e) {
      log.warn("Failed to update process status cache: " + e.getMessage());
    }

    try {
      this.sendMessage2WebSocket(
          this.history_id + BaseTool.log_separator + "Exit Code: " + exitvalue);
    } catch (Exception e) {
      // In CLI mode, this might fail but we should continue
      log.debug("Error sending exit code message (expected in CLI mode): " + e.getMessage());
    }
  }

  /**
   * Updates the status and logs for an execution.
   *
   * @param logs The logs generated during execution.
   * @param status The execution status (e.g., "Done" or "Failed").
   */
  public void updateStatus(String logs, String status) {

    History h = ht.getHistoryById(this.history_id);

    if (BaseTool.isNull(h)) {

      h = new History();

      h.setHistory_id(history_id);

      log.debug("This is very unlikely");
    }

    if (ExecutionStatus.DONE.equals(status)
        || ExecutionStatus.FAILED.equals(status)
        || ExecutionStatus.STOPPED.equals(status)
        || ExecutionStatus.SKIPPED.equals(status)) {

      h.setHistory_end_time(BaseTool.getCurrentSQLDate());
    }

    h.setHistory_output(logs);

    h.setIndicator(status);

    ht.saveHistory(h);
    
    // Update the cache with the status if available
    // This is important for CLI mode where the cache might be used for status tracking
    try {
      if (processStatusCache != null) {
        processStatusCache.updateStatus(history_id, status);
        log.debug("Updated cache for history ID: " + history_id + " with status: " + status);
      }
    } catch (Exception e) {
      log.warn("Failed to update process status cache: " + e.getMessage());
      // Continue execution even if cache update fails
    }
  }

  /**
   * The `run` method is executed when a new thread for the `LocalSessionOutput` class is started.
   * This method handles the capture of command execution output and WebSocket communication.
   */
  @Override
  public void run() {
    // Initialize StringBuffer for storing pre-log content and the logs generated during execution
    StringBuffer prelog = new StringBuffer(); // The part that is generated before the WebSocket session is started
    StringBuffer logs = new StringBuffer();

    try {
      log.info("Local session output thread started"); // Log that the local session output thread has started

      // Initialize counters and statuses for monitoring and logging
      int linenumber = 0; // Line number of the current output
      int startrecorder = -1; // Records the starting line number when the output is null
      int nullnumber = 0; // Counts consecutive null output lines

      // Update the status of the executed command as "Running" in the history record
      this.updateStatus("Running", "Running");
      
      // Update the cache with the running status
      if (processStatusCache != null) {
        processStatusCache.updateStatus(history_id, ExecutionStatus.RUNNING);
      }

      // Send a message to the WebSocket indicating that the process has started
      try {
        this.sendMessage2WebSocket(
            this.history_id + BaseTool.log_separator + "Process " + this.history_id + " Started");
      } catch (Exception e) {
        // In CLI mode, this might fail but we should continue processing
        log.debug("Error sending start message (expected in CLI mode): " + e.getMessage());
      }

      String line = null; // Initialize a variable to store each line of output

      // Read output lines until they are null (command execution is finished)
      while ((line = in.readLine()) != null) {
        try {
          log.info(line);
          // refreshLogMonitor(); // Check and refresh the WebSocket session

          // readLine will block if nothing to send
          if (BaseTool.isNull(in)) {
            log.debug("Local Session Output Reader is closed prematurely.");
            break;
          }

          linenumber++; // Increment the line number

          if (linenumber % 1 == 0) {
            this.updateStatus(logs.toString(), "Running");
          }

          // When null output lines are detected, track them to determine if the command is finished
          if (BaseTool.isNull(line)) {
            // If ten consecutive output lines are null, consider it disconnected
            if (startrecorder == -1) startrecorder = linenumber;
            else nullnumber++;

            if (nullnumber == 10) {
              if ((startrecorder + nullnumber) == linenumber) {
                log.debug("Null output lines exceed 10. Disconnected.");

                // Don't set status to Done here - wait for process exit code
                // The status will be set based on exit code after the loop
                log.info("Detected disconnection (10 null lines), will check process exit code");
                break;
              } else {
                startrecorder = -1;
                nullnumber = 0;
              }
            }
          } else if (line.contains("==== Geoweaver Bash Output Finished ====")) {
            // Handle specific marker lines if present
          } else {
            //						log.info("Local output " + theprocess.pid() + " >> " + line + " - token: " +
            // token); // Log each line of output
            logs.append(line).append("\n"); // Append the line to the logs

            // Always try to send via WebSocket with automatic reconnection
            // First check if we have any buffered content in prelog
            if (prelog.length() > 0) {
              line = prelog.toString() + line;
              prelog = new StringBuffer();
            }
            
            // Send the message - our improved sendMessage2WebSocket will try to reconnect if needed
            try {
              this.sendMessage2WebSocket(this.history_id + BaseTool.log_separator + line);
              
              // If we're using the fallback mechanism (determined in sendMessage2WebSocket),
              // log this information for debugging purposes
              if (useWebSocketFallback) {
                log.debug("Using long polling fallback for message delivery to history_id: " + this.history_id);
              }
            } catch (Exception e) {
              // In CLI mode, this might fail but we should continue processing
              log.debug("Error sending message to WebSocket (expected in CLI mode): " + e.getMessage());
            }
          }
        } catch (Exception e) {
          e.printStackTrace();
          // Depending on the language, update the status to "Failed"
          this.updateStatus(logs.toString(), "Failed");
          
          // Update the cache with the failed status
          if (processStatusCache != null) {
            processStatusCache.updateStatus(history_id, ExecutionStatus.FAILED);
          }
          break;
        } finally {
          // session.saveHistory(logs.toString()); //write the failed record
        }
      }

      // Get the exit code first before setting status
      int exitCode = -1; // Default to -1 (unknown) to avoid incorrectly marking failed processes as successful
      String finalStatus = ExecutionStatus.FAILED; // Default to Failed to be safe
      
      // If the process is available, get its exit code
      if (!BaseTool.isNull(theprocess)) {
        try {
          // Wait for the process to finish if it's still alive
          if (theprocess.isAlive()) {
            log.info("Process is still alive, waiting for it to complete...");
            theprocess.waitFor();
          }
          
          exitCode = theprocess.exitValue();
          log.info("Process exit code: " + exitCode);
          
          // Set status based on exit code
          if (exitCode == 0) {
            finalStatus = ExecutionStatus.DONE;
            log.info("Process completed successfully (exit code: 0)");
          } else {
            finalStatus = ExecutionStatus.FAILED;
            log.warn("Process failed with exit code: " + exitCode);
          }
        } catch (Exception e) {
          e.printStackTrace();
          log.error("Error getting process exit code: " + e.getLocalizedMessage());
          // If we can't get the exit code, assume failure to be safe
          finalStatus = ExecutionStatus.FAILED;
        }
      } else {
        log.warn("Process object is null, cannot determine exit code. Assuming failure to be safe.");
        finalStatus = ExecutionStatus.FAILED;
      }
      
      // Update status based on exit code
      this.updateStatus(logs.toString(), finalStatus);
      
      // Update the cache with the final status
      if (processStatusCache != null) {
        processStatusCache.updateStatus(history_id, finalStatus);
      }
      
      // Also call endWithCode to ensure history is properly updated
      if (exitCode != -1) {
        this.endWithCode(token, exitCode);
      } else {
        // If we couldn't get exit code, still update history with failed status
        History h = ht.getHistoryById(this.history_id);
        if (h != null) {
          h.setIndicator(ExecutionStatus.FAILED);
          h.setHistory_end_time(BaseTool.getCurrentSQLDate());
          ht.saveHistory(h);
          log.warn("Updated history to Failed due to inability to determine exit code");
        }
      }

      // Send a message to the WebSocket indicating that the process has finished
      try {
        this.sendMessage2WebSocket(
            this.history_id + BaseTool.log_separator + "The process " + history_id + " is finished.");
      } catch (Exception e) {
        // In CLI mode, this might fail but we've already completed the process
        log.debug("Error sending completion message (expected in CLI mode): " + e.getMessage());
      }

      // This thread will end by itself when the task is finished; you don't have to close it
      // manually
      // GeoweaverController.sessionManager.closeByToken(token); // Close the session by token

      log.info("Local session output thread ended");
    } catch (Exception e) {
      e.printStackTrace();
      // Depending on the language, update the status to "Failed"
      this.updateStatus(logs.toString() + "\n" + e.getLocalizedMessage(), "Failed");
      
      // Update the cache with the failed status
      if (processStatusCache != null) {
        processStatusCache.updateStatus(history_id, ExecutionStatus.FAILED);
      }
    } finally {
      try {
        this.sendMessage2WebSocket(
            this.history_id
                + BaseTool.log_separator
                + "======= Process "
                + this.history_id
                + " ended");
      } catch (Exception e) {
        // In CLI mode, this might fail but we've already logged the end
        log.debug("Error sending end message (expected in CLI mode): " + e.getMessage());
      }
    }
  }
}
