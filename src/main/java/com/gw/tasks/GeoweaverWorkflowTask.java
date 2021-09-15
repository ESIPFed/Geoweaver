package com.gw.tasks;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.server.WorkflowServlet;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.WorkflowTool;
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
	WorkflowTool wt;

	@Autowired
	TaskManager tm;
	
	String name;
	
	String wid, mode;
	
	String[] hosts;
	
	String[] pswds;
	
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
	
	
	public void initialize(String history_id, String wid, String mode, String[] hosts, String[] pswds, String token) {
		
		this.history_id = history_id;

		this.wid = wid;
		
		this.mode = mode;
		
		this.hosts = hosts;
		
		this.pswds = pswds;
		
		this.token = token;
		
		this.name = "GW-Workflow-Run-" + token;
		
		this.startMonitor(token);
		
	}
	
	
	public void saveWorkflowHistory() {
		
		this.history_end_time = bt.getCurrentSQLDate();
		
		this.history_end_time = bt.getCurrentSQLDate();
		
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

		if(bt.isNull(se)){

			log.error("The monitor should never be empty");

		}else{

			log.debug("Find workflow-socket session - " + se.getId());

			monitor = se;
			
		}

		// wt.token2ws.put(token, socketsession.getId());
		
	}

	public void refreshMonitor(){

		if(bt.isNull(monitor)){

			monitor = WorkflowServlet.findSessionByToken(token); 
			
		}
		

	}
	

	/**
	 * Send status message back to websocket end
	 * @param nodes
	 * @param flags
	 */
	public void sendStatus(JSONArray nodes, String[] flags) {
		
		try {
			
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
				
				this.refreshMonitor();
				log.debug("Send workflow process status back to the client: " + array);
//				monitor.sendMessage(new TextMessage(array.toJSONString()));
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
			
			if(!bt.isNull(monitor))
				monitor.getBasicRemote().sendText("{\"workflow_status\": \"completed\", \"workflow_history_id\":\""+this.history_id+"\"}");
			// if(!bt.isNull(monitor))
			// 	monitor.close();
			
			// wt.token2ws.remove(token);
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	public void execute() {
		// TODO Auto-generated method stub
		
		log.debug(" + + + start Geoweaver workflow " + wid + " - history id : " + this.history_id);
		
		try {
			
			//get the nodes and edges of the workflows
			
			this.history_process = wid;
			
			this.history_begin_time = bt.getCurrentSQLDate();
			
			this.history_input = "";
			
			this.history_output = "";
			
			Workflow w = wt.getById(wid);
			
			if(bt.isNull(w))
				throw new RuntimeException("no workflow is found");
			
			//execute the process in a while loop - for now. Improve this in future
			
			int executed_process = 0;
			
			JSONParser parser = new JSONParser();
			
			JSONArray edges = (JSONArray)parser.parse(w.getEdges());
			
			JSONArray nodes = (JSONArray)parser.parse(w.getNodes());
			
			String[] flags = new String[nodes.size()];
			
			for(int i=0;i<flags.length; i++ ) {
				
				flags [i] = ExecutionStatus.READY;
				
			}

			for(int i=0;i<nodes.size();i++){

				((JSONObject)nodes.get(i)).put("history_id", new RandomString(11).nextString()); //generate history id before call the execution function
			}
			
			// all the ids in this map is history id
			Map<String, List> node2condition = wt.getNodeConditionMap(nodes, edges);
			
			// while(executed_process < (nodes.size())) {
			for(int i=0;i< nodes.size();i++){
				
				//find next process to execute - the id has two parts: process type id - process object id
				
				// String[] idnum = wt.findNextProcess(node2condition, flags, nodes);
				
				// String nextid = idnum[0];

				String nextid = (String)((JSONObject)nodes.get(i)).get("id");

				String nexthistoryid = (String)((JSONObject)nodes.get(i)).get("history_id");
				
				log.debug("this round is : " + nextid);
				
				String stat = ExecutionStatus.READY;
				
				wt.updateNodeStatus(nextid, flags, nodes, stat);
				
				sendStatus(nodes, flags);

				this.history_indicator = ExecutionStatus.READY;
				
				// String processTypeId = nextid.split("-")[0];
				
				int num = i;
				
				String hid = mode.equals("one")?hosts[0]:hosts[num];
				
				String password = mode.equals("one")?pswds[0]:pswds[num];
				
				//nodes
//				[{"title":"download-landsat","id":"nhi96d-7VZhh","x":119,"y":279},{"title":"filter_cloud","id":"rh1u8q-4sCmg","x":286,"y":148},{"title":"filter_shadow","id":"rpnhlg-JZfyQ","x":455,"y":282},{"title":"match_cdl_landsat","id":"omop8l-1p5x1","x":624,"y":152}]
				
				//edges
//				[{"source":{"title":"sleep5s","id":"ac4724-jL0Ep","x":342.67081451416016,"y":268.8715720176697},"target":{"title":"testbash","id":"199vsg-Xr6FZ","x":465.2892303466797,"y":41.6651611328125}},{"source":{"title":"testbash","id":"199vsg-oAq2d","x":-7.481706619262695,"y":180.70700073242188},"target":{"title":"sleep5s","id":"ac4724-jL0Ep","x":342.67081451416016,"y":268.8715720176697}}]
				
				try {

					GeoweaverProcessTask new_task = BeanTool.getBean(GeoweaverProcessTask.class);

					new_task.initialize(nexthistoryid, nextid, hid, password, token, true, null, null, null, this.history_id); //what is token?

					new_task.setPreconditionProcesses(node2condition.get(nexthistoryid));

					new_task.setWorkflowHistoryId(this.history_id);

					log.debug("Precondition number: " + node2condition.get(nexthistoryid).size());
					
					tm.addANewTask(new_task);

					// String resp = pt.execute(processTypeId, hid, password, token, true, null, null, null); //need update the null to be python environment
					
					// JSONObject respobj = (JSONObject)new JSONParser().parse(resp);
					
					// member_historyid = (String)respobj.get("history_id");
					
					
				}catch(Exception e) {
					
					stat = ExecutionStatus.FAILED;

					wt.updateNodeStatus(nextid, flags, nodes, stat);
				
					sendStatus(nodes, flags);
					
					e.printStackTrace();
				}
				
				this.history_input += nextid + ";";
				
				this.history_output += nexthistoryid + ";";
				
//				pid2hid.put(nextid, historyid); //save the mapping between process id and history id
				
				// wt.updateNodeStatus(nextid, flags, nodes, stat); //once the process is finished, updated its status
				
				executed_process++;
				
			}
			
			sendStatus(nodes, flags); //last message
			
			log.info("workflow execution is triggered.");

			this.history_indicator = ExecutionStatus.RUNNING;
			
			saveWorkflowHistory();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		} finally {
			
			// GeoweaverController.sessionManager.closeWebSocketByToken(token); //close ssh output transferring websocket at the end
			
			// stopMonitor(); //shut down workflow status monitor websocket
			
		}

	}

}
