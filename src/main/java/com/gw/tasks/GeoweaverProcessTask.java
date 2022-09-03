package com.gw.tasks;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.websocket.Session;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import  com.gw.server.CommandServlet;
import com.gw.server.WorkflowServlet;
import com.gw.tools.FileTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.ExecutionTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 
 * Task for builtin processes
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
public class GeoweaverProcessTask  extends Task {
	
	@Autowired
	HostTool ht;
	
	@Autowired
	ProcessTool pt;

	@Autowired
	ExecutionTool et;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	FileTool ft;
	
	@Autowired
	HistoryTool hist;

	@Autowired
	TaskManager tm;
	
	String	   pid;

	String     workflow_pid;
		   
	String	   host;
		   
	String	   pswd;
		   
	String	   token;
	
	boolean	   isjoin;
		   
   	String	   history_input;
    
    String	   history_output;
    
    Date	   history_begin_time;
    
    Date	   history_end_time;
    
    String 	   history_id;

	String     bin;

	String     pyenv;

	String     basedir;

	String     curstatus;

	// A list of history id
	// only run this process when all the listed history_id is finished or failed
	List       precondition_processes; //only for workflow's member process

	boolean    isReady;

	String     workflow_history_id;
    
    @Value("${geoweaver.upload_file_path}")
    String     upload_file_path;
    
    Logger logger = Logger.getLogger(this.getClass());
	
	/**
	 * This monitor is used to return the logs while process is running
	 */
    javax.websocket.Session monitor = null;

	/**
	 * This monitor is used to return the status of the process in a running workflow
	 */
	javax.websocket.Session workflow_monitor = null;
    
    public GeoweaverProcessTask() {
    	
    	//for spring
    	
    }

	public String getWorkflowHistoryId(){

		return this.workflow_history_id;
	}

	public void setWorkflowHistoryId(String workflow_history_id){

		this.workflow_history_id = workflow_history_id;

	}

	public void setIsReady(boolean isReady){

		this.isReady = isReady;

	}

	public boolean getIsReady(){

		return this.isReady;
	}

	public List getPreconditionProcesses(){

		return this.precondition_processes;

	}

	public void setPreconditionProcesses(List precondition_processes){

		this.precondition_processes = precondition_processes;
	}

	/**
	 * This is a temporary solution, history id should be one of the initialized parameter
	 */
	public void setHistoryID(String newid){

		this.history_id = newid;

	}
	
	public void initialize(String history_id, String pid, String host, String pswd, String token, boolean isjoin, String bin, String pyenv, String basedir, String workflow_history_id) {
		
		if(pid.contains("-")){
			
			this.workflow_pid = pid;

			this.pid = this.workflow_pid.split("-")[0];

		}else{

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

		if(!BaseTool.isNull(ws)) this.startMonitor(ws);

		Session workflow_ws = WorkflowServlet.findSessionByToken(token);

		if(!BaseTool.isNull(workflow_ws) && !BaseTool.isNull(this.workflow_history_id))
			this.workflow_monitor = workflow_ws; //hook up the workflow session
		
		this.curstatus = ExecutionStatus.READY;

	}

	/**
	 * Sometimes the old websocket session is closed and the client opened a new one
	 * This function will capture the change and switch to the new channel
	 */
	public void refreshWorkflowMonitor(){

		// if(!BaseTool.isNull(this.workflow_monitor) && !this.workflow_monitor.isOpen()){
		this.workflow_monitor = WorkflowServlet.findSessionByToken(token);
		// }

	}
	
	public String getHistory_id() {
		return history_id;
	}
	
	/**
	 * Start the monitoring of the task
	 * @param socketsession
	 */
	public void startMonitor(javax.websocket.Session socketsession) {
		
		monitor = socketsession;
		
	}

	/**
	 * This function is called when the task is not loaded by a worker
	 */
	public void endPrematurely(){
		
		this.curstatus = ExecutionStatus.STOPPED;

		updateEverything();

	}
	
	/**
	 * Stop the monitoring of the task
	 */
	public void stopMonitor() {
		
		//no closing anymore, the websocket session between client and server should be always active
		this.monitor = null;
		this.workflow_monitor = null;
		
//		try {
//			
//			logger.info("close the websocket session from server side");
//			
////			if(!BaseTool.isNull(monitor))
////				monitor.close();
//			
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//			
//		}
		
	}

	@Override
	public void initialize() {
		
		
	}

	@Override
	public void execute() {

		logger.debug(" + + + start Geoweaver Process " + pid );
		
		try {

			Thread.sleep(1); //sleep 1s to wait for the client to catch up
			
			//get the nodes and edges of the workflows
			
			this.history_begin_time = BaseTool.getCurrentSQLDate();
			
			this.history_output = "";

			this.curstatus = ExecutionStatus.RUNNING;

			this.updateEverything();

			et.executeProcess(history_id, pid, host, pswd, token, isjoin, bin, pyenv, basedir);

			this.curstatus = ExecutionStatus.DONE;
			
		}catch(Exception e) {
			
			e.printStackTrace();

			this.curstatus = ExecutionStatus.FAILED;
			
			this.history_output = e.getLocalizedMessage();

			
		}finally {
			
			if(!isjoin) this.stopMonitor(); //if run solo, close. if workflow, don't.
			
		}
		
			
	}

	/**
	 * Send all tasks' status of the same workflow
	 * @param id
	 * @param history_id
	 * @param flag
	 */
	public void sendAllTaskStatus(){

		try {

			if(!BaseTool.isNull(this.workflow_history_id)) {

				History wf = hist.getHistoryById(workflow_history_id);

				String[] member_process_id_list = wf.getHistory_input().split(";");

				String[] member_history_id_list = wf.getHistory_output().split(";");
			
				JSONArray array = new JSONArray();

				String workflow_status = ExecutionStatus.DONE; //check if all the member processes of workflow have been finished
				
				int errorcheck = 0;

				for(int i=0;i<member_history_id_list.length;i++) {
					
					String c_history_id = member_history_id_list[i];

					History c_his = hist.getHistoryById(c_history_id);

					JSONObject obj = new JSONObject();

					obj.put("id", member_process_id_list[i]);

					obj.put("history_id", c_history_id);
					
					obj.put("status", c_his.getIndicator());

					if(BaseTool.isNull(c_his.getIndicator()) 
						|| ExecutionStatus.READY.equals(c_his.getIndicator()) 
						|| ExecutionStatus.RUNNING.equals(c_his.getIndicator())){

							workflow_status = ExecutionStatus.RUNNING;
						}else if(ExecutionStatus.FAILED.equals(c_his.getIndicator())){
							errorcheck = 1;
						}
					
					array.add(obj);
					
				}


//				monitor.sendMessage(new TextMessage(array.toJSONString()));
				sendMessage2WorkflowWebsocket(array.toJSONString());

				if(errorcheck==1 && ExecutionStatus.DONE.equals(workflow_status)){

					workflow_status = ExecutionStatus.FAILED;

				}

				//update workflow status
				this.history_end_time = BaseTool.getCurrentSQLDate();
				wf.setHistory_end_time(this.history_begin_time);
				wf.setIndicator(workflow_status);

				hist.saveHistory(wf);
				if(ExecutionStatus.DONE.equals(workflow_status) 
					|| ExecutionStatus.FAILED.equals(workflow_status) 
					|| ExecutionStatus.STOPPED.equals(workflow_status)){
						sendMessage2WorkflowWebsocket("{\"workflow_status\": \"completed\", \"workflow_history_id\": \""+workflow_history_id+"\"}");
				}
				
				
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}

	}

	/**
	 * This seems not working well because of the conflicts of parallel processed tasks
	 * @param id
	 * @param history_id
	 * @param flag
	 */
	public void sendSingleTaskStatus(String id, String history_id, String flag) {
		
		try {
			
			if(this.workflow_monitor!=null) {
				
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
		
		this.history_end_time = BaseTool.getCurrentSQLDate();
		
		History history = hist.getHistoryById(this.history_id);
		
		if(ExecutionStatus.RUNNING.equals(history.getHistory_output())){

			bt.sleep(1); //wait for 1 seconds
			history = hist.getHistoryById(this.history_id);
		}

		history.setHistory_begin_time(this.history_begin_time);
		
		history.setHistory_end_time(this.history_end_time);
		
		history.setHistory_process(this.pid);
		
		if(!BaseTool.isNull(this.history_input) && BaseTool.isNull(history.getHistory_input())) history.setHistory_input(this.history_input);
		
		if(!BaseTool.isNull(this.history_output)) history.setHistory_output(this.history_output); //save the error message to the output
		//if the process is already failed, don't update the status again because it is already failed
		if(!ExecutionStatus.FAILED.equals(history.getIndicator()))history.setIndicator(this.curstatus.toString());
		
		history.setHost_id(this.host);
		
		if(!ExecutionStatus.RUNNING.equals(history.getHistory_output())) hist.saveHistory(history); //only save if the historyoutput is not Running.
    	
	}

	/**
	 * Update the database history table and notify the workflow websocket session
	 */
	public void updateEverything(){

		saveHistory();

		if(!BaseTool.isNull(this.workflow_history_id)){

			refreshWorkflowMonitor();

			// this.sendSingleTaskStatus(workflow_pid, history_id, this.curstatus);
			this.sendAllTaskStatus();

		}

	}

	@Override
	public void responseCallback() {

		logger.debug("Process "+ this.history_id +" is finished!");

		logger.info("What is the history output? " + this.history_output);

		this.updateEverything();

		tm.done(this);
		
		//this is optional to avoid thread conflict
		// sendMessage2LogoutWebsocket("Process " + pid + " - History ID - " + history_id + " finished.");
		
		//notify the task list observer
		// setChanged();
		// notifyObservers(this);
		
	}

	void sendMessage2WorkflowWebsocket(String msg){

		if(workflow_monitor!=null){
			
			synchronized(workflow_monitor){

					try {
						workflow_monitor.getBasicRemote().sendText(msg);
					} catch (Exception e) {
						e.printStackTrace();
					}


			}

		}
	}

	void sendMessage2LogoutWebsocket(String msg){

		synchronized(monitor){

			if(monitor!=null){
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

		this.updateEverything();

		tm.done(this);
		//notify the task list observer
		// setChanged();
		// notifyObservers(this);

		//this is optional to avoid thread conflict
		// sendMessage2LogoutWebsocket("Process " + pid + " - History ID - " + history_id + " failed.");
		
		
	}

	@Override
	public String getName() {
		return "New-Process-Task-" + this.pid + "-" + this.history_id;
	}


}
