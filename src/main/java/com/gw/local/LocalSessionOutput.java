package com.gw.local;

import java.io.BufferedReader;
import java.io.IOException;

import javax.websocket.Session;

import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

/**
 * Service class for managing local session output and interaction with WebSocket.
 * Implements the Runnable interface to run in a separate thread.
 */
@Service
@Scope("prototype")
public class LocalSessionOutput implements Runnable {

    @Autowired
    BaseTool bt;

    @Autowired
    HistoryTool ht;

    @Autowired
    HistoryRepository historyRepository;

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
    
    /**
     * Default constructor for Spring.
     */
    public LocalSessionOutput() {
        // This constructor is used for Spring.
    }
    
    /**
     * Initializes the LocalSessionOutput with necessary parameters for running.
     *
     * @param in            BufferedReader for reading the session's output.
     * @param token         The session token.
     * @param history_id    The history ID associated with the session.
     * @param lang          The programming language used in the session.
     * @param jupyterfilepath The Jupyter file path, if applicable.
     */
    public void init(BufferedReader in, String token, String history_id, String lang, String jupyterfilepath) {
        log.info("LocalSessionOutput created for token "+ token);
        this.in = in;
        this.token = token;
        this.run = true;
        this.history_id = history_id;
        this.lang = lang;
        this.jupyterfilepath = jupyterfilepath;
        refreshLogMonitor();
    }
    
    /**
     * Stops the local session output processing.
     */
    public void stop() {
        run = false;
    }

    /**
     * Sends a message to the associated WebSocket session.
     *
     * @param msg The message to be sent to the WebSocket.
     */
    public void sendMessage2WebSocket(String msg) {
        try {
            this.wsout = CommandServlet.findSessionById(token);
            if (!BaseTool.isNull(wsout)){
                if(wsout.isOpen()) {
                    wsout.getBasicRemote().sendText(msg);
                }else{
                    CommandServlet.removeSessionById(token);
                }
            }else{
                log.warn(String.format("cannot find websocket for token %s", token));
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
    }

    /**
     * Refreshes the log monitor for WebSocket interaction.
     * If the WebSocket session is null or closed, it attempts to retrieve the session.
     */
    public void refreshLogMonitor() {
        if (BaseTool.isNull(wsout) || !wsout.isOpen()) {
            wsout = CommandServlet.findSessionById(token);
        }
    }

	/**
	 * Cleans the WebSocket session by removing it from the CommandServlet.
	 */
	public void cleanLogMonitor() {
		CommandServlet.removeSessionById(history_id);
	}

	/**
	 * Sets the associated process for this LocalSessionOutput.
	 *
	 * @param p The process to be associated with this session output.
	 */
	public void setProcess(Process p) {
		this.theprocess = p;
	}

	/**
	 * Ends the process with an exit code and updates the history accordingly.
	 *
	 * @param token     The session token.
	 * @param exitvalue The exit code of the process.
	 */
	public void endWithCode(String token, int exitvalue) {
		this.stop();

		// Get the latest history
		History h = ht.getHistoryById(this.history_id);

		if (exitvalue == 0) {
			h.setIndicator(ExecutionStatus.DONE);
		} else {
			h.setIndicator(ExecutionStatus.FAILED);
		}

		h.setHistory_end_time(BaseTool.getCurrentSQLDate());

		ht.saveHistory(h);

		this.sendMessage2WebSocket(this.history_id + BaseTool.log_separator + "Exit Code: " + exitvalue);
	}

	/**
	 * Updates the status and logs for a Jupyter execution.
	 *
	 * @param logs   The logs generated during execution.
	 * @param status The execution status (e.g., "Done" or "Failed").
	 */
	public void updateJupyterStatus(String logs, String status) {
		History h = ht.getHistoryById(this.history_id);

		if (BaseTool.isNull(h)) {
			h = new History();
			h.setHistory_id(history_id);
			log.debug("This is very unlikely");
		}

		String resultjupyterjson = bt.readStringFromFile(this.jupyterfilepath);

		h.setHistory_input(resultjupyterjson);
		h.setHistory_output(logs);
		h.setIndicator(status);

		if ("Done".equals(status) || "Failed".equals(status)) {
			h.setHistory_end_time(BaseTool.getCurrentSQLDate());
		}

		ht.saveHistory(h);
	}

	/**
	 * Updates the status and logs for an execution.
	 *
	 * @param logs   The logs generated during execution.
	 * @param status The execution status (e.g., "Done" or "Failed").
	 */
	public void updateStatus(String logs, String status) {

		History h = ht.getHistoryById(this.history_id);

		if(BaseTool.isNull(h)){

			h = new History();

			h.setHistory_id(history_id);

			log.debug("This is very unlikely");
		}

		if(ExecutionStatus.DONE.equals(status) || ExecutionStatus.FAILED.equals(status) 
				|| ExecutionStatus.STOPPED.equals(status) || ExecutionStatus.SKIPPED.equals(status)){

			h.setHistory_end_time(BaseTool.getCurrentSQLDate());

		}

		h.setHistory_output(logs);

		h.setIndicator(status);

		ht.saveHistory(h);
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
			int linenumber = 0;  // Line number of the current output
			int startrecorder = -1; // Records the starting line number when the output is null
			int nullnumber = 0; // Counts consecutive null output lines
	
			// Update the status of the executed command as "Running" in the history record
			this.updateStatus("Running", "Running");
	
			// Send a message to the WebSocket indicating that the process has started
			this.sendMessage2WebSocket(this.history_id + BaseTool.log_separator+ "Process " + this.history_id + " Started");
	
			String line = null; // Initialize a variable to store each line of output
	
			// Read output lines until they are null (command execution is finished)
			while ((line = in.readLine()) != null) {
				try {
					log.info("this is when line is read: " + line);
					refreshLogMonitor(); // Check and refresh the WebSocket session
	
					// readLine will block if nothing to send
					if (BaseTool.isNull(in)) {
						log.debug("Local Session Output Reader is closed prematurely.");
						break;
					}
	
					linenumber++; // Increment the line number

					if(linenumber%1==0){
						this.updateStatus(logs.toString(), "Running");
					}
	
					// When null output lines are detected, track them to determine if the command is finished
					if (BaseTool.isNull(line)) {
						// If ten consecutive output lines are null, consider it disconnected
						if (startrecorder == -1)
							startrecorder = linenumber;
						else
							nullnumber++;
	
						if (nullnumber == 10) {
							if ((startrecorder + nullnumber) == linenumber) {
								log.debug("Null output lines exceed 10. Disconnected.");
	
								// Depending on the language (e.g., "jupyter"), update the status
								if ("jupyter".equals(this.lang)) {
									this.updateJupyterStatus(logs.toString(), "Done");
								} else {
									this.updateStatus(logs.toString(), "Done");
								}
								break;
							} else {
								startrecorder = -1;
								nullnumber = 0;
							}
						}
					} else if (line.contains("==== Geoweaver Bash Output Finished ====")) {
						// Handle specific marker lines if present
					} else {
						log.info("Local output " + theprocess.pid() + " >> " + line + " - token: " + token); // Log each line of output
						logs.append(line).append("\n"); // Append the line to the logs
	
						if (!BaseTool.isNull(wsout) && wsout.isOpen()) {
							if (prelog.toString() != null) {
								line = prelog.toString() + line;
								prelog = new StringBuffer();
							}
							this.sendMessage2WebSocket(this.history_id + BaseTool.log_separator + line);
						} else {
							prelog.append(line).append("\n"); // Append to the prelog if WebSocket isn't available
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					// Depending on the language, update the status to "Failed"
					if ("jupyter".equals(this.lang)) {
						this.updateJupyterStatus(logs.toString(), "Failed");
					} else {
						this.updateStatus(logs.toString(), "Failed");
					}
					break;
				} finally {
					// session.saveHistory(logs.toString()); //write the failed record
				}
			}
	
			// Depending on the language (e.g., "jupyter"), update the status to "Done"
			if ("jupyter".equals(this.lang)) {
				this.updateJupyterStatus(logs.toString(), "Done");
			} else {
				this.updateStatus(logs.toString(), "Done");
			}
	
			// If the process is available, attempt to stop and get its exit code
			if (!BaseTool.isNull(theprocess)) {
				try {
					log.info("This output thread corresponding process: " + theprocess.pid());
					if (theprocess.isAlive()) theprocess.destroy();
					this.endWithCode(token, theprocess.exitValue());
				} catch (Exception e) {
					e.printStackTrace();
					log.error("the process doesn't end well" + e.getLocalizedMessage());
				}
			}
	
			// Send a message to the WebSocket indicating that the process has finished
			this.sendMessage2WebSocket(this.history_id + BaseTool.log_separator+ "The process " + history_id + " is finished.");
	
			// This thread will end by itself when the task is finished; you don't have to close it manually
			// GeoweaverController.sessionManager.closeByToken(token); // Close the session by token
	
			log.info("Local session output thread ended");
		} catch (Exception e) {
			e.printStackTrace();
			// Depending on the language, update the status to "Failed"
			this.updateStatus(logs.toString() + "\n" + e.getLocalizedMessage(), "Failed");
		} finally {
			this.sendMessage2WebSocket(this.history_id + BaseTool.log_separator + "======= Process " + this.history_id + " ended");
		}
	}
	

    

}
