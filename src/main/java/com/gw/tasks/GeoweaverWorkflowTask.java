package com.gw.tasks;

import com.gw.tools.WorkflowTool;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import com.gw.database.WorkflowRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.server.WorkflowServlet;
import com.gw.tools.EnvironmentTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class GeoweaverWorkflowTask{
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	HistoryTool hist;
	
	@Autowired
	WorkflowRepository workflowRepository;

	@Autowired
	EnvironmentTool et;

	@Autowired
	TaskManager tm;
	
	String name;
	
	String wid, mode;
	
	String[] hosts;
	
	String[] pswds;

	String[] envs;
	
	String token;
	
	Session monitor = null;
	
	/**********************************************/
    /** section of the geoweaver history records **/
    /**********************************************/
	
//	Map 			 pid2hid = new HashMap();
	
    String			 history_input;
    
    String			 history_output;
    
    Date			 history_begin_time;
    
    Date			 history_end_time;
    
    String			 history_process;
    
    String			 history_id;

	String           history_indicator;
    
    /**********************************************/
    /** end of history section **/
    /**********************************************/
    
    public GeoweaverWorkflowTask() {
    	
    	//for spring
    	
    }
	
	public GeoweaverWorkflowTask(String name) {
		
		this.name = name;
		
	}
	
	public String getHistory_id() {
		return history_id;
	}
	
	
	public void initialize(String history_id, String wid, String mode, String[] hosts, String[] pswds, String[] envs, String token) {
		
		this.history_id = history_id;

		this.wid = wid;
		
		this.mode = mode;
		
		this.hosts = hosts;
		
		this.pswds = pswds;

		this.envs = envs;
		
		this.token = token;
		
		this.name = "GW-Workflow-Run-" + token;
		
		this.startMonitor(token);
		
	}
	
	
	public void saveWorkflowHistory() {
		
		this.history_end_time = BaseTool.getCurrentSQLDate();
		
		this.history_end_time = BaseTool.getCurrentSQLDate();
		
		History history = hist.getHistoryById(this.history_id);
		
		history.setHistory_begin_time(this.history_begin_time);
		
		history.setHistory_end_time(this.history_end_time);
		
		history.setHistory_process(this.history_process);
		
		history.setHistory_input(this.history_input);
		
		history.setHistory_output(this.history_output);
		
		history.setHost_id(bt.array2String(hosts, ";"));

		history.setIndicator(this.history_indicator.toString());
		
		hist.saveHistory(history);
    	
	}
	
	/**
	 * Start the monitoring of the task
	 * @param socketsession
	 */
	public void startMonitor(String token) {
		
		Session se = WorkflowServlet.findSessionByToken(token);

		if(BaseTool.isNull(se)){

			log.debug("The monitor is empty, might be in command line mode.");

		}else{

			log.debug("Find workflow-socket session - " + se.getId());

			monitor = se;
			
		}

	}

	public void refreshMonitor(){

		monitor = WorkflowServlet.findSessionByToken(token); 

	}
	

	/**
	 * Send status message back to websocket end
	 * @param nodes
	 * @param flags
	 */
	public void sendStatus(JSONArray nodes, String[] flags) {
		
		try {

			refreshMonitor();
			
			if(monitor!=null) {
				
				JSONArray array = new JSONArray();
				
				for(int i=0;i<nodes.size();i++) {
					
					String id = (String)((JSONObject)nodes.get(i)).get("id");

					String history_id = (String)((JSONObject)nodes.get(i)).get("history_id");
					
					JSONObject obj = new JSONObject();
					
					obj.put("id", id);

					obj.put("history_id", history_id);
					
					obj.put("status", flags[i].toString());
					
					array.add(obj);
					
				}
				
				log.debug("Send workflow process status back to the client: " + array);
				
				monitor.getBasicRemote().sendText(array.toJSONString());
				
			}
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
	}
	
	/**
	 * Stop the monitoring of the task
	 */
	public void stopMonitor() {
		
		try {
			log.info("close the websocket session from server side");
			
			if(!BaseTool.isNull(monitor))
				monitor.getBasicRemote().sendText("{\"workflow_status\": \"completed\", \"workflow_history_id\":\""+this.history_id+"\"}");
			// if(!BaseTool.isNull(monitor))
			// 	monitor.close();
			
			// wt.token2ws.remove(token);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	public Map<String, List> getNodeConditionMap(JSONArray nodes, JSONArray edges) throws ParseException{
		
		//find the condition nodes of each process
		
		Map<String, List> node2condition = new HashMap();
		
		for(int i=0;i<nodes.size();i++) {
			
			String current_id = (String)((JSONObject)nodes.get(i)).get("id");

			String current_history_id = (String)((JSONObject)nodes.get(i)).get("history_id");
			
			List preids = new ArrayList();
			
			for(int j=0;j<edges.size();j++) {
				
				JSONObject eobj = (JSONObject)edges.get(j);
				
				String sourceid = (String)((JSONObject)eobj.get("source")).get("id");
				
				String targetid = (String)((JSONObject)eobj.get("target")).get("id");
				
				if(current_id.equals(targetid)) {

					preids.add(getNodeByID(nodes, sourceid).get("history_id"));
					
					// preids.add(sourceid);
					
				}
				
				
			}

			node2condition.put(current_history_id, preids);
			
		}
		
		return node2condition;
		
	}

	public JSONObject getNodeByID(JSONArray nodes, String id){

		JSONObject theobj = null;

		for(int i=0;i<nodes.size();i++){

			String current_id = (String)((JSONObject)nodes.get(i)).get("id");

			if(current_id.equals(id)){

				theobj = (JSONObject)nodes.get(i);
				break;

			}

		}

		return theobj;

	}

	/**
	 * Update the status of a node
	 * @param id
	 * @param flags
	 * @param nodes
	 * @param status
	 */
	public boolean updateNodeStatus(String id, String[] flags, JSONArray nodes, String status) {

		boolean isFailed = false;

		for(int j=0;j<nodes.size();j++) {

			String prenodeid = (String)((JSONObject)nodes.get(j)).get("id");

			if(prenodeid.equals(id)) {
				flags[j] = status;
				if ("FAILED".equals(status)) {
					isFailed = true;
				}
				break;
			}
		}
		return isFailed;
	}

	public void execute() {
		log.debug(" + + + start Geoweaver workflow " + wid + " - history id : " + this.history_id);

		try {
			// Initialize workflow details
			this.history_process = wid;
			this.history_begin_time = BaseTool.getCurrentSQLDate();
			this.history_input = "";
			this.history_output = "";

			// Retrieve workflow information
			Workflow w = workflowRepository.findById(wid).get();
			if (BaseTool.isNull(w))
				throw new RuntimeException("No workflow is found");

			JSONParser parser = new JSONParser();
			JSONArray edges = (JSONArray) parser.parse(w.getEdges());
			JSONArray nodes = (JSONArray) parser.parse(w.getNodes());

			// Initialize node status flags
			String[] flags = new String[nodes.size()];
			for (int i = 0; i < flags.length; i++) {
				flags[i] = ExecutionStatus.READY;
			}

			// Generate history ID for each node
			for (int i = 0; i < nodes.size(); i++) {
				((JSONObject) nodes.get(i)).put("history_id", new RandomString(11).nextString());
			}

			// Set initial status of the workflow
			this.history_indicator = ExecutionStatus.READY;
			Map<String, List> node2condition = this.getNodeConditionMap(nodes, edges);

			for (int i = 0; i < nodes.size(); i++) {
				String nextid = String.valueOf(((JSONObject) nodes.get(i)).get("id"));
				String nexthistoryid = String.valueOf(((JSONObject) nodes.get(i)).get("history_id"));
				String skip = String.valueOf(((JSONObject) nodes.get(i)).get("skip"));

				log.debug("This round is: " + nextid);

				String hid = mode.equals("one") ? hosts[0] : hosts[i];
				String password = mode.equals("one") ? pswds[0] : pswds[i];
				String envid = mode.equals("one") ? envs[0] : envs[i];

				try {
					if ("true".equals(skip)) {
						hist.saveSkippedHisotry(nexthistoryid, nextid, hid);
						this.updateNodeStatus(nextid, flags, nodes, ExecutionStatus.SKIPPED);
					} else {
						GeoweaverProcessTask new_task = BeanTool.getBean(GeoweaverProcessTask.class);
						Environment env = et.getEnvironmentById(envid);
						if (BaseTool.isNull(env)) {
							new_task.initialize(nexthistoryid, nextid, hid, password, token, true, null, null, null, this.history_id);
						} else {
							new_task.initialize(nexthistoryid, nextid, hid, password, token, true, env.getBin(), env.getPyenv(), env.getBasedir(), this.history_id);
						}
						new_task.setPreconditionProcesses(node2condition.get(nexthistoryid));
						log.debug("Precondition number: " + node2condition.get(nexthistoryid).size());
						tm.addANewTask(new_task);
					}

					sendStatus(nodes, flags);

					// Check if the process failed
					if (updateNodeStatus(nextid, flags, nodes, ExecutionStatus.FAILED)) {
						WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);
						wt.stop(wid);
						throw new RuntimeException("Process " + nextid + " failed. Stopping workflow execution.");
					}

				} catch (Exception e) {
					this.updateNodeStatus(nextid, flags, nodes, ExecutionStatus.FAILED);
					sendStatus(nodes, flags);
					e.printStackTrace();
					throw e; // Re-throw the exception to stop the workflow
				}

				this.history_input += nextid + ";";
				this.history_output += nexthistoryid + ";";
			}

			sendStatus(nodes, flags); // Send final status
			log.info("Workflow execution is triggered.");
			this.history_indicator = ExecutionStatus.RUNNING;
			saveWorkflowHistory();

		} catch (Exception e) {
			e.printStackTrace();
			// Additional error handling as needed
		} finally {
			// Clean-up actions
			// GeoweaverController.sessionManager.closeWebSocketByToken(token);
			// stopMonitor();
		}
	}


}
