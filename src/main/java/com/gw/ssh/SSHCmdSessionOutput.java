package com.gw.ssh;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
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

  @Autowired BaseTool bt;

  @Autowired HistoryTool ht;

  public SSHCmdSessionOutput() {

    // for spring

  }

  public void init(BufferedReader in, String token, String history_id) {

    log.info("created");
    this.in = in;
    this.token = token;
    this.run = true;
    this.history_id = history_id;
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

    if (exitvalue == 0) {

      h.setIndicator(ExecutionStatus.DONE);

    } else {

      h.setIndicator(ExecutionStatus.FAILED);
    }

    h.setHistory_end_time(BaseTool.getCurrentSQLDate());

    ht.saveHistory(h);

    log.info("Exit code: " + exitvalue);

    CommandServlet.sendMessageToSocket(
        token, history_id + BaseTool.log_separator + "Exit Code: " + exitvalue);
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
    int exit_code = 0;
    try {
      // Split the line based on the equals sign
      String[] parts = last_line.split("=");
  
      if (parts.length == 2) {
          String key = parts[0];  // gw_exit_code
          String value = parts[1]; // 1
          
          // Check if the key is "gw_exit_code"
          if (key.equals("gw_exit_code")) {
              exit_code = Integer.parseInt(value);
              System.out.println("The key is 'gw_exit_code' and code is " + value);
          }
      } else {
          System.err.println("Invalid line format: " + last_line);
      }
    } catch (Exception e) {
        System.err.println("Error processing line: " + last_line);
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
        token, history_id + BaseTool.log_separator + "Process " + this.history_id + " Started");

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

      if(exit_code == 0){

        this.updateStatus(logs.toString(), "Done");

      }else{

        this.updateStatus(logs.toString(), "Failed");

      }
      

      CommandServlet.sendMessageToSocket(
          token,
          history_id + BaseTool.log_separator + "The process " + this.history_id + " is finished.");

    } catch (Exception e) {

      e.printStackTrace();

      updateStatus(logs.toString(), "Failed");

    } finally {

      CommandServlet.sendMessageToSocket(
          token,
          history_id + BaseTool.log_separator + "======= Process " + this.history_id + " ended");
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
