package com.gw.tasks;

import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;
import com.gw.server.WorkflowServlet;
import com.gw.tools.ExecutionTool;
import com.gw.tools.FileTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.ProcessStatusCache;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.websocket.Session;
import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * Task for builtin processes
 *
 * @author JensenSun
 */
@Service
@Scope("prototype")
public class GeoweaverProcessTask extends Task {

  @Autowired HostTool ht;

  @Autowired ProcessTool pt;

  @Autowired ExecutionTool et;

  @Autowired BaseTool bt;

  @Autowired FileTool ft;

  @Autowired HistoryTool hist;

  @Autowired HistoryRepository hr;

  @Autowired TaskManager tm;
  
  @Autowired ProcessStatusCache processStatusCache;

  String pid;

  String workflow_pid;

  String host;

  String pswd;

  String token;

  boolean isjoin;

  String history_input;

  String history_output;

  Date history_begin_time;

  Date history_end_time;

  String history_id;

  String bin;

  String pyenv;

  String basedir;

  String curstatus;

  // A list of history id
  // only run this process when all the listed history_id is finished or failed
  List precondition_processes; // only for workflow's member process

  boolean isReady;

  // should stop this task as all its precondition processes are complete or failed
  boolean shouldPass;

  String workflow_history_id;

  @Value("${geoweaver.upload_file_path}")
  String upload_file_path;

  Logger logger = Logger.getLogger(this.getClass());

  /** This monitor is used to return the logs while process is running */
  javax.websocket.Session monitor = null;

  /** This monitor is used to return the status of the process in a running workflow */
  javax.websocket.Session workflow_monitor = null;

  public GeoweaverProcessTask() {

    // for spring

  }

  public String getWorkflowHistoryId() {

    return this.workflow_history_id;
  }

  public void setWorkflowHistoryId(String workflow_history_id) {

    this.workflow_history_id = workflow_history_id;
  }

  public void setIsReady(boolean isReady) {

    this.isReady = isReady;
  }

  public boolean getIsReady() {

    return this.isReady;
  }

  public List getPreconditionProcesses() {

    return this.precondition_processes;
  }

  public void setPreconditionProcesses(List precondition_processes) {

    this.precondition_processes = precondition_processes;
  }

  /**
   * Check if a task is ready to execute
   *
   * @param thet
   * @return
   */
  public boolean checkIfReady() {

    this.isReady = false;

    List prehistoryid = this.getPreconditionProcesses();

    if (!BaseTool.isNull(prehistoryid) && prehistoryid.size() > 0) {

      int check = 0;

      for (int i = 0; i < prehistoryid.size(); i++) {
        String historyId = (String) prehistoryid.get(i);
        // Always use cache for status checks during workflow execution
        String current_status = processStatusCache.getStatus(historyId);
        
        // If not in cache, we need to check the database once
        if (current_status == null) {
          Optional<History> ho = hr.findById(historyId);

          if (ho.isPresent()) {
            current_status = ho.get().getIndicator();
            // Update cache with status from database for future checks
            processStatusCache.updateStatus(historyId, current_status);
            logger.debug("Updated cache from database for history ID: " + historyId + ": " + current_status);
          } else {
            check = 1;
            break;
          }
        } else {
          logger.debug("Using cached status for history ID: " + historyId + ": " + current_status);
        }

        if (BaseTool.isNull(current_status)
            || current_status.equals(ExecutionStatus.RUNNING)
            || current_status.equals(ExecutionStatus.READY)) {
          check = 1;
          break;
        }
      }

      if (check == 0) this.isReady = true;

    } else {
      
      this.isReady = true;

    }

    return this.isReady;
  }

  public boolean checkShouldPassOrNot() {

    this.shouldPass = false;

    List prehistoryid = this.getPreconditionProcesses();

    if (!BaseTool.isNull(prehistoryid) && prehistoryid.size() > 0) {

      int check = 0;

      for (int i = 0; i < prehistoryid.size(); i++) {
        String historyId = (String) prehistoryid.get(i);
        // Always use cache for status checks during workflow execution
        String current_status = processStatusCache.getStatus(historyId);
        
        // If not in cache, we need to check the database once
        if (current_status == null) {
          Optional<History> ho = hr.findById(historyId);

          if (ho.isPresent()) {
            current_status = ho.get().getIndicator();
            // Update cache with status from database for future checks
            processStatusCache.updateStatus(historyId, current_status);
            logger.debug("Updated cache from database for history ID: " + historyId + ": " + current_status);
          } else {
            continue;
          }
        } else {
          logger.debug("Using cached status for history ID: " + historyId + ": " + current_status);
        }

        if (BaseTool.isNull(current_status)
            || current_status.equals(ExecutionStatus.FAILED)
            || current_status.equals(ExecutionStatus.STOPPED)) {
          check = 1;
          break;
        }
      }

      if (check == 1) this.shouldPass = true;

    }

    return this.shouldPass;
  }

  /** This is a temporary solution, history id should be one of the initialized parameter */
  public void setHistoryID(String newid) {

    this.history_id = newid;
  }

  public void initialize(
      String history_id,
      String pid,
      String host,
      String pswd,
      String token,
      boolean isjoin,
      String bin,
      String pyenv,
      String basedir,
      String workflow_history_id) {

    if (pid.contains("-")) {

      this.workflow_pid = pid;

      this.pid = this.workflow_pid.split("-")[0];

    } else {

      this.pid = pid;
    }

    this.workflow_history_id = workflow_history_id;

    this.host = host;

    this.pswd = pswd;

    this.token = token;

    this.history_id = history_id;

    this.isjoin = isjoin;

    this.bin = bin;

    this.pyenv = pyenv;

    this.basedir = basedir;

    Session ws = CommandServlet.findSessionById(token);

    // if(bt==null) bt = new BaseTool();

    if (!BaseTool.isNull(ws)) this.startMonitor(ws);

    Session workflow_ws = WorkflowServlet.findSessionByToken(token);

    if (!BaseTool.isNull(workflow_ws) && !BaseTool.isNull(this.workflow_history_id))
      this.workflow_monitor = workflow_ws; // hook up the workflow session

    this.curstatus = ExecutionStatus.READY;
  }

  /**
   * Sometimes the old websocket session is closed and the client opened a new one This function
   * will capture the change and switch to the new channel
   */
  public void refreshWorkflowMonitor() {

    // if(!BaseTool.isNull(this.workflow_monitor) && !this.workflow_monitor.isOpen()){
    this.workflow_monitor = WorkflowServlet.findSessionByToken(token);
    // }

  }

  public String getHistory_id() {
    return history_id;
  }

  /**
   * Start the monitoring of the task
   *
   * @param socketsession
   */
  public void startMonitor(javax.websocket.Session socketsession) {

    monitor = socketsession;
  }

  /** This function is called when the task is not loaded by a worker */
  public void endPrematurely() {

    this.curstatus = ExecutionStatus.STOPPED;

    updateEverything();
  }

  /** Stop the monitoring of the task */
  public void stopMonitor() {

    // no closing anymore, the websocket session between client and server should be always active
    this.monitor = null;

    this.workflow_monitor = null;
  }

  @Override
  public void initialize() {}

  @Override
  public void execute() {
    
    logger.debug(" + + + start Geoweaver Process " + pid);

    try {

      // Thread.sleep(500); // sleep 1s to wait for the client to catch up

      // get the nodes and edges of the workflows

      this.history_begin_time = BaseTool.getCurrentSQLDate();

      this.history_output = "";

      this.curstatus = ExecutionStatus.RUNNING;
      
      processStatusCache.updateStatus(history_id, this.curstatus);

      this.updateEverything();

      et.executeProcess(history_id, pid, host, pswd, token, isjoin, bin, pyenv, basedir);

      this.curstatus = ExecutionStatus.DONE;
      
    } catch (Exception e) {

      e.printStackTrace();

      this.curstatus = ExecutionStatus.FAILED;
      
      // Update the cache with the new status
      processStatusCache.updateStatus(history_id, this.curstatus);

      this.history_output = e.getLocalizedMessage();

    } finally {

      this.updateEverything();

      tm.done(this);

      if (!isjoin) this.stopMonitor(); // if run solo, close. if workflow, don't.
    }

    System.out.println(
        String.format(
            "> Fnished process: %s - history: %s - Status: %s",
            pid, history_id, this.curstatus.toString()));
  }

  /**
   * Send all tasks' status of the same workflow
   *
   * @param id
   * @param history_id
   * @param flag
   */
  public void sendWorkflowTaskStatus() {

    try {

      if (!BaseTool.isNull(this.workflow_history_id)) {

        // Get workflow data from database only to retrieve the process and history lists
        // This is necessary as we need the structure information
        History wf = hist.getHistoryById(workflow_history_id);

        String[] member_process_id_list = wf.getHistory_input().split(";");

        String[] member_history_id_list = wf.getHistory_output().split(";");

        JSONArray array = new JSONArray();

        String workflow_status =
            ExecutionStatus
                .DONE; // check if all the member processes of workflow have been finished

        int errorcheck = 0;

        for (int i = 0; i < member_history_id_list.length; i++) {

          String c_history_id = member_history_id_list[i];

          JSONObject obj = new JSONObject();

          obj.put("id", member_process_id_list[i]);

          obj.put("history_id", c_history_id);

          // Always use cache for status - never query database for status during workflow execution
          String c_history_status = processStatusCache.getStatus(c_history_id);
          
          obj.put("status", c_history_status);

          if (BaseTool.isNull(c_history_status)
                || ExecutionStatus.READY.equals(c_history_status)
                || ExecutionStatus.RUNNING.equals(c_history_status)) {

              workflow_status = ExecutionStatus.RUNNING;

          } else if (ExecutionStatus.FAILED.equals(c_history_status)) {

            errorcheck = 1; //mark it but don't interrupt other processes

          }

          array.add(obj);

        }
        
        sendMessage2WorkflowWebsocket(array.toJSONString());

        if (errorcheck == 1) {

          workflow_status = ExecutionStatus.FAILED;
        }

        // Update workflow status in cache first
        processStatusCache.updateStatus(workflow_history_id, workflow_status);
        
        // Then update database
        this.history_end_time = BaseTool.getCurrentSQLDate();
        wf.setHistory_end_time(this.history_begin_time);
        wf.setIndicator(workflow_status);

        hist.saveHistory(wf);

        if (ExecutionStatus.DONE.equals(workflow_status)
            || ExecutionStatus.FAILED.equals(workflow_status)
            || ExecutionStatus.STOPPED.equals(workflow_status)
            || ExecutionStatus.SKIPPED.equals(workflow_status)) {
          sendMessage2WorkflowWebsocket(
              "{\"workflow_status\": \"completed\", \"workflow_history_id\": \""
              + workflow_history_id+ "\", " 
              +"\"execution_final_status\": \"" + workflow_status
              + "\"}");
        }
      }

    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  /**
   * This seems not working well because of the conflicts of parallel processed tasks
   *
   * @param id
   * @param history_id
   * @param flag
   */
  public void sendSingleTaskStatus(String id, String history_id, String flag) {

    try {

      if (this.workflow_monitor != null) {

        JSONObject obj = new JSONObject();

        obj.put("message_type", "single_process");

        obj.put("id", id);

        obj.put("history_id", history_id);

        obj.put("status", flag);

        //				monitor.sendMessage(new TextMessage(array.toJSONString()));
        sendMessage2WorkflowWebsocket(obj.toJSONString());
      }

    } catch (Exception e) {

      e.printStackTrace();
    }
  }

  public void saveHistory() {

    // this.history_end_time = BaseTool.getCurrentSQLDate();

    // History history = hist.getHistoryById(this.history_id);

    // history.setHistory_begin_time(this.history_begin_time);

    // history.setHistory_end_time(this.history_end_time);

    // history.setHistory_process(this.pid);

    // if (!BaseTool.isNull(this.history_input) && BaseTool.isNull(history.getHistory_input()))
    //   history.setHistory_input(this.history_input);

    // if (!BaseTool.isNull(this.history_output))
    //   history.setHistory_output(this.history_output); // save the error message to the output
    // // if the process is already failed, don't update the status again because it is already failed
    // if (!ExecutionStatus.FAILED.equals(history.getIndicator()))
    //   history.setIndicator(this.curstatus.toString());

    // history.setHost_id(this.host);

    // if (!ExecutionStatus.RUNNING.equals(history.getHistory_output())) {
    //   hist.saveHistory(history); // only save if the historyoutput is not Running.
    // }
  }

  /** Update the database history table and notify the workflow websocket session */
  public void updateEverything() {

    // saveHistory();

    logger.info(
        "updateeverything is called and this.workflow_history_id = " + this.workflow_history_id);

    // refreshWorkflowMonitor();

    // this.sendSingleTaskStatus(workflow_pid, history_id, this.curstatus);
    this.sendWorkflowTaskStatus();
  }

  @Override
  public void responseCallback() {

    logger.debug("Process " + this.history_id + " is finished!");

    logger.info("What is the history output? " + this.history_output);

    this.updateEverything();

    tm.done(this);

    // this is optional to avoid thread conflict
    // sendMessage2LogoutWebsocket("Process " + pid + " - History ID - " + history_id + "
    // finished.");

    // notify the task list observer
    // setChanged();
    // notifyObservers(this);

  }

  /**
   * Sends a message to the workflow WebSocket or long polling channel based on the configured default channel.
   * Uses the WorkflowServlet's sendMessageToSocket method which handles the channel selection and fallback.
   *
   * @param msg The message to send
   */
  void sendMessage2WorkflowWebsocket(String msg) {
    try {
      // Use the WorkflowServlet's sendMessageToSocket method which handles channel selection
      // This will automatically use the configured default channel (WebSocket or long polling)
      WorkflowServlet.sendMessageToSocket(token, msg);
    } catch (Exception e) {
      // In CLI mode, we might not have access to the web components
      // Just log the message instead of trying to send it through web channels
      logger.info("CLI Mode - Workflow message: " + msg);
      logger.debug("Error sending workflow message (expected in CLI mode): " + e.getMessage());
    }
  }

  void sendMessage2LogoutWebsocket(String msg) {

    synchronized (monitor) {
      if (monitor != null) {
        try {
          monitor.getBasicRemote().sendText(msg);
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }
  }

  @Override
  public void failureCallback(Exception e) {

    logger.error("Process execution is failed " + e.getLocalizedMessage());

    this.curstatus = ExecutionStatus.FAILED;
    
    // Update the cache with the new status
    processStatusCache.updateStatus(history_id, this.curstatus);

    this.updateEverything();

    tm.done(this);
    // notify the task list observer
    // setChanged();
    // notifyObservers(this);

    // this is optional to avoid thread conflict
    // sendMessage2LogoutWebsocket("Process " + pid + " - History ID - " + history_id + " failed.");

  }

  @Override
  public String getName() {
    return "New-Process-Task-" + this.pid + "-" + this.history_id;
  }
}
