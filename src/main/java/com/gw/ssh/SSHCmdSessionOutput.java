package com.gw.ssh;

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
 * This class is for command line output
 *
 * @author JensenSun
 */
@Service
@Scope("prototype")
public class SSHCmdSessionOutput implements Runnable {

  protected Logger log = LoggerFactory.getLogger(getClass());

  protected BufferedReader in;

  protected WebSocketSession out; // log&shell websocket - not used any more

  protected Session wsout;

  protected String token; // session token

  protected boolean run = true;

  protected String history_id;
  
  protected String hostInfo; // Store host information for logging

  @Autowired BaseTool bt;

  @Autowired HistoryTool ht;
  
  @Autowired ProcessStatusCache processStatusCache;

  public SSHCmdSessionOutput() {

    // for spring

  }

  public void init(BufferedReader in, String token, String history_id) {
    init(in, token, history_id, null);
  }
  
  public void init(BufferedReader in, String token, String history_id, String hostInfo) {

    log.info("created");
    this.in = in;
    this.token = token;
    this.run = true;
    this.history_id = history_id;
    this.hostInfo = hostInfo != null ? hostInfo : "Remote Host";
    wsout = CommandServlet.findSessionById(token);
  }

  public void stop() {

    run = false;
  }

  /**
   * End process with exit code
   *
   * @param token
   * @param exitvalue
   */
public void endWithCode(String token, String history_id, int exitvalue) {

    History h = ht.getHistoryById(history_id);
    
    if (h == null) {
      log.error("History record not found for history_id: " + history_id + ", cannot set exit code");
      return;
    }

    String status;
    if (exitvalue == 0) {
      status = ExecutionStatus.DONE;
      log.info("Process completed successfully on " + hostInfo + " (exit code: 0)");
    } else {
      status = ExecutionStatus.FAILED;
      log.warn("Process failed on " + hostInfo + " (exit code: " + exitvalue + ")");
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

    log.info("Exit code: " + exitvalue + " for history ID: " + history_id + " on " + hostInfo);

    CommandServlet.sendMessageToSocket(
        token, history_id + BaseTool.log_separator + "Exit Code: " + exitvalue + " (on " + hostInfo + ")");
  }

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
  }

  int get_exit_code_from_log_line(String last_line){
    // Default to -1 (unknown) instead of 0 (success) to avoid incorrectly marking failed processes as successful
    int exit_code = -1;
    try {
      if (BaseTool.isNull(last_line) || last_line.trim().isEmpty()) {
        log.warn("Cannot extract exit code from empty or null line");
        return exit_code;
      }
      
      // Split the line based on the equals sign
      String[] parts = last_line.split("=");
  
      if (parts.length == 2) {
          String key = parts[0].trim();  // gw_exit_code
          String value = parts[1].trim(); // 1
          
          // Check if the key is "gw_exit_code"
          if (key.equals("gw_exit_code")) {
              exit_code = Integer.parseInt(value);
              log.info("Extracted exit code from log line: " + exit_code);
          } else {
              log.debug("Line does not contain gw_exit_code, key: " + key);
          }
      } else {
          log.debug("Line format does not match expected pattern (key=value): " + last_line);
      }
    } catch (Exception e) {
        log.error("Error processing exit code from line: " + last_line);
        log.error("Exception: " + e.getMessage());
        e.printStackTrace();
    }
    return exit_code;
  }

  @Override
  public void run() {

    log.info("SSH session output thread started");

    StringBuffer prelog =
        new StringBuffer(); // the part that is generated before the WebSocket session is started

    StringBuffer logs = new StringBuffer();

    int linenumber = 0;

    int startrecorder = -1;

    int nullnumber = 0;

    updateStatus("Running", "Running"); // initiate the history record

    CommandServlet.sendMessageToSocket(
        token, history_id + BaseTool.log_separator + "Process " + this.history_id + " Started on " + hostInfo);

    String line = null;

    try {

      int exit_code = 0;
      String last_non_null_line = "";

      while ((line = in.readLine()) != null) {

        // readLine will block if nothing to send

        linenumber++;

        // when detected the command is finished, end this process
        if (BaseTool.isNull(line)) {

          // if ten consecutive output lines are null, break this loop

          if (startrecorder == -1) startrecorder = linenumber;
          else nullnumber++;

          if (nullnumber == 10) {

            if ((startrecorder + nullnumber) == linenumber) {

              log.debug("null output lines exceed 10. Disconnected.");

              break;

            } else {

              startrecorder = -1;

              nullnumber = 0;
            }
          }
        }else{
          last_non_null_line = line;
        }

        log.debug("command thread output >> " + line);

        logs.append(line).append("\n");

        if (linenumber % 1 == 0) {
          this.updateStatus(logs.toString(), "Running");
        }

        // Always attempt to send the log line regardless of WebSocket status
        // This will use the configured communication channel (polling or websocket)
        if (prelog.toString() != null && !prelog.toString().isEmpty()) {
          line = prelog.toString() + line;
          prelog = new StringBuffer();
        }

        CommandServlet.sendMessageToSocket(token, history_id + BaseTool.log_separator + line);

        // If message couldn't be sent, store it in prelog buffer for next attempt
        if (!BaseTool.isNull(wsout) && !wsout.isOpen()) {
          wsout = CommandServlet.findSessionById(token);
          if (BaseTool.isNull(wsout) || !wsout.isOpen()) {
            prelog.append(line).append("\n");
          }
        }
      }

      exit_code = get_exit_code_from_log_line(last_non_null_line);

      // Only update status if we successfully extracted exit code from log
      // If exit_code is -1 (unknown), it means endWithCode() should have been called
      // or will be called, so we should not override the status here
      if (exit_code == 0) {
        log.info("Process completed successfully (exit code: 0)");
        this.updateStatus(logs.toString(), "Done");
      } else if (exit_code > 0) {
        log.warn("Process failed with exit code: " + exit_code);
        this.updateStatus(logs.toString(), "Failed");
      } else {
        // exit_code is -1 (unknown), check if status was already set by endWithCode()
        History h = ht.getHistoryById(history_id);
        if (h != null) {
          String currentStatus = h.getIndicator();
          if (BaseTool.isNull(currentStatus) || ExecutionStatus.RUNNING.equals(currentStatus)) {
            // Status not set yet, assume failure if we can't determine exit code
            log.warn("Cannot determine exit code from logs, but status not set. Assuming failure to be safe.");
            this.updateStatus(logs.toString(), "Failed");
          } else {
            log.info("Status already set to: " + currentStatus + ", not overriding");
            // Just update the logs, don't change the status
            this.updateStatus(logs.toString(), currentStatus);
          }
        } else {
          log.error("History record not found for history_id: " + history_id);
          // If history doesn't exist, mark as failed to be safe
          this.updateStatus(logs.toString(), "Failed");
        }
      }
      

      CommandServlet.sendMessageToSocket(
          token,
          history_id + BaseTool.log_separator + "The process " + this.history_id + " is finished on " + hostInfo + ".");

    } catch (Exception e) {

      e.printStackTrace();

      updateStatus(logs.toString(), "Failed");

    } finally {

      CommandServlet.sendMessageToSocket(
          token,
          history_id + BaseTool.log_separator + "======= Process " + this.history_id + " ended on " + hostInfo + " =======");
    }

    // GeoweaverController.sessionManager.closeByToken(token);

    log.info("SSH session output thread ended");
  }

  public void sendMessage2WebSocket(String msg) {

    if (!BaseTool.isNull(wsout)) {

      synchronized (wsout) {
        try {
          if (wsout.isOpen()) wsout.getBasicRemote().sendText(msg);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  public void setWebSocketSession(WebSocketSession session) {
    log.info("received websocket session");
  }
}
