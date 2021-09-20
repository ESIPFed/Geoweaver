package com.gw.tools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.HistoryRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tasks.GeoweaverWorkflowTask;
import com.gw.tasks.TaskManager;
import com.gw.utils.RandomString;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author JensenSun
 *
 */
@Service
public class WorkflowTool {
	
	public Map<String, String> token2ws = new HashMap();
	
	private Logger logger = Logger.getLogger(WorkflowTool.class);
	
	
	@Autowired
	WorkflowRepository workflowrepository;
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Autowired
	TaskManager tm;
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	HistoryTool tool;

	@Autowired
	UserTool ut;

	@Autowired
	BaseTool bt;
	
	@Autowired
	GeoweaverWorkflowTask task;
	
	/**
	 * For Andrew
	 * @param history_id
	 * @return
	 */
	public String stop(String history_id) {
        
        History whis = historyrepository.findById(history_id).get();
        
        String childprocesses = whis.getHistory_output();
        
        String[] child_process_ids = childprocesses.split(";");
        
        for(String cid : child_process_ids) {
        	
        	History phis = historyrepository.findById(cid).get();
        	
        	// pt.stop(phis.getHistory_id());

			tm.stopTask(phis.getHistory_id());
        	
        }

		whis.setIndicator(ExecutionStatus.STOPPED);

		historyrepository.save(whis);

		String resp = "{\"history_id\": \""+history_id+
//					
//					"\", \"token\": \""+token+
//					
					"\", \"ret\": \"stopped\"}";

        return resp;
	}
	
	public String toJSON(Workflow w) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(w);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}
	
	public String list(String owner){
		
		// Iterator<Workflow> wit = workflowrepository.findAll().iterator();
		
		Iterator<Workflow> wit = workflowrepository.findAllPublicPrivateByOwner(owner).iterator();
		
		StringBuffer json = new StringBuffer("[");
		
		int num = 0;
		
		while(wit.hasNext()) {
			
			Workflow w = wit.next();
			
			if( num++ != 0) {
				
				json.append(",");
				
			}
			
			json.append(toJSON(w));
			
		}
		
		json.append("]");
		
		return json.toString();
		
	}
	
	public Workflow getById(String id) {
		
		Workflow w = workflowrepository.findById(id).get();
		
		return w;
		
	}
	
	public String detail(String id) {
		
		Workflow wf = workflowrepository.findById(id).get();
		
		return toJSON(wf);
		
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

	
	
	/**
	 * Find a process whose status is not executed, while all of its condition nodes are satisfied. 
	 * @param nodemap
	 * @param flags
	 * @param nodes
	 * @return
	 */
	public String[] findNextProcess(Map<String, List> nodemap, ExecutionStatus[] flags, JSONArray nodes) {
		
		String id = null;
		
		String num = null;
		
		for(int i=0;i<nodes.size();i++) {
			
			String currentid = (String)((JSONObject)nodes.get(i)).get("id");
			
			if(checkNodeStatus(currentid, flags, nodes).equals(ExecutionStatus.READY)) {
				
				continue;
				
			}
			
			List prenodes = nodemap.get(currentid);
			
			boolean satisfied = true;
			
			//check if all the prenodes are satisfied
			
			for(int j=0;j<prenodes.size();j++) {
				
				String prenodeid = (String)prenodes.get(j);
				
				//if any of the pre- nodes is not satisfied, this node is passed. 
				
				if(checkNodeStatus(prenodeid, flags, nodes).equals(ExecutionStatus.DONE)
						&&checkNodeStatus(prenodeid, flags, nodes).equals(ExecutionStatus.FAILED)) {
					
					satisfied = false;
					
					break;
					
				}
				
			}
			
			if(satisfied) {
				
				id = currentid;
				
				num = String.valueOf(i);
				
				break;
				
			}
			
		}
		
		String[] ret = new String[] {id, num};
		
		return ret;
		
	}
	
	/**
	 * Update the status of a node
	 * @param id
	 * @param flags
	 * @param nodes
	 * @param status
	 */
	public void updateNodeStatus(String id, String[] flags, JSONArray nodes, String status) {
		
		for(int j=0;j<nodes.size();j++) {
			
			String prenodeid = (String)((JSONObject)nodes.get(j)).get("id");
			
			if(prenodeid.equals(id)) {
				
				flags[j] = status;
				
				break;
				
			}
			
		}
		
	}

	public List<Workflow> getAllWorkflow(){

		List<Workflow> wlist = new ArrayList();

		workflowrepository.findAll().forEach(w->wlist.add(w));

		return wlist;

	}

	public void save(Workflow w){

		workflowrepository.save(w);

	}
	
	/**
	 * Check the status of a node
	 * @param id
	 * @param flags
	 * @param nodes
	 * @return
	 */
	private ExecutionStatus checkNodeStatus(String id, ExecutionStatus[] flags, JSONArray nodes) {
		
		ExecutionStatus status = null;
		
		for(int j=0;j<nodes.size();j++) {
			
			String nodeid = (String)((JSONObject)nodes.get(j)).get("id");
			
			if(nodeid.equals(id)) {
				
				status = flags[j];
				
				break;
				
			}
			
		}
		
		return status;
		
	}

	/**
	 * Execute a workflow
	 * @param id
	 * @param mode
	 * @param hosts
	 * @param pswd
	 * @param token
	 * @return
	 */
	public String execute(String history_id, String wid, String mode, String[] hosts, String[] pswds, String httpsessionid) {
		
		//use multiple threads to execute the processes
		
		String resp = null;
		
		try {
			
//			GeoweaverWorkflowTask task = new GeoweaverWorkflowTask("GW-Workflow-Run-" + token);
			
			// task.initialize(id, mode, hosts, pswds, token);
			
			// tm.addANewTask(task);

			

			task.initialize(history_id, wid, mode, hosts, pswds, httpsessionid);

			task.execute();

			resp = "{\"history_id\": \""+task.getHistory_id()+
					
					"\", \"token\": \""+httpsessionid+
					
					"\", \"ret\": \"success\"}";
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		} 
        		
		return resp;
		
	}
	
	/**
	 * Update workflow nodes and edges
	 * @param wid
	 * @param nodes
	 * @param edges
	 */
	public void update(String wid, String nodes, String edges) {
		
//		StringBuffer sql = new StringBuffer("update abstract_model set process_connection = ?, param_connection = ? where identifier = '");
//		
//		sql.append(wid).append("'; ");
//		
//		DataBaseOperation.preexecute(sql.toString(), new String[] {nodes, edges});
		
		Workflow wf = workflowrepository.findById(wid).get();
		
		wf.setNodes(nodes);
		
		wf.setEdges(edges);
		
		workflowrepository.save(wf);
		
		
	}

	
	public String add(String name, String nodes, String edges, String ownerid) {
		
		String newid = new RandomString(20).nextString();
		
		Workflow wf = new Workflow();
		
		wf.setId(newid);
		
		wf.setName(name);
		
		wf.setEdges(edges);
		
		wf.setNodes(nodes);
		
		wf.setOwner(ownerid);
		
		workflowrepository.save(wf);
		
//		logger.info("name: " + name + "\nnodes: " + nodes + "\nedges: " + edges);
//		
//		StringBuffer sql = new StringBuffer("insert into abstract_model (identifier, name, namespace, process_connection, param_connection) values ('");
//		
//		sql.append(newid).append("', '");
//		
//		sql.append(name).append("', 'http://geoweaver.csiss.gmu.edu/workflow/");
//		
//		sql.append(name).append("', ?, ? )");
//		
//		DataBaseOperation.preexecute(sql.toString(), new String[] {nodes, edges});
		
		return newid;
		
	}
	
	public String del(String workflowid) {
		
//		StringBuffer sql = new StringBuffer("delete from abstract_model where identifier = '").append(workflowid).append("';");
//		
//		DataBaseOperation.execute(sql.toString());
		
		workflowrepository.deleteById(workflowid);
		
		return "done";
		
	}
	
	/**
	 * Get all active processes
	 * @return
	 */
	public String all_active_process() {
		
		StringBuffer resp = new StringBuffer() ;
		
//		StringBuffer sql = new StringBuffer("select * from history, abstract_model where history.process = abstract_model.identifier and indicator = 'Running' ORDER BY begin_time DESC;");
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		List<Object[]> active_his_workflow = historyrepository.findRunningWorkflow();
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			for(;num<active_his_workflow.size();num++) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
			
				Object[] hiscols = active_his_workflow.get(num);
				
				resp.append("{ \"id\": \"").append(hiscols[0]).append("\", ");
				
				resp.append("\"begin_time\": \"").append(hiscols[1]).append("\", ");
				
				resp.append("\"end_time\": \"").append(hiscols[2]).append("\", ");
				
				resp.append("\"status\": \"").append(pt.escape(String.valueOf(hiscols[3]))).append("\", ");
				
				resp.append("\"output\": \"").append(hiscols[4]).append("\"}");
				
			}
			
			resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
		
	}

	/**
	 * show the history of every execution of the workflow
	 * @param string
	 * @return
	 */
	public String all_history(String workflow_id) {
		
		
		return tool.workflow_all_history(workflow_id);
		
	}
	
	/**
	 * List to JSON
	 * @param list
	 * @return
	 */
	public String list2JSON(String list) {
		
		StringBuffer json = new StringBuffer("[");
		
		String[] ps = list.split(";");
		
		for(int i=0;i<ps.length;i++) {
			
			if(i!=0) {
				
				json.append(",");
				
			}
			
			json.append("\"").append(ps[i]).append("\"");			
		}
		
		json.append("]");
		
		return json.toString();
		
	}
	
	public String recent(int limit) {
		
		StringBuffer resp = new StringBuffer();
		
//		StringBuffer sql = new StringBuffer("select * from history, abstract_model where abstract_model.identifier = history.process ORDER BY begin_time DESC limit ").append(limit).append(";");
//
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			List<Object[]> recent_his_workflow = historyrepository.findRecentWorkflow(limit);
			
			resp.append("[");
			
			int num = 0;
			
			for(;num<recent_his_workflow.size();num++) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				Object[] recent_his = recent_his_workflow.get(num);
				
				resp.append("{ \"id\": \"").append(recent_his[0]).append("\", "); //history id
				
				resp.append("\"name\": \"").append(recent_his[12]).append("\", ");
				
				resp.append("\"end_time\": \"").append(recent_his[2]).append("\", ");
				
				resp.append("\"begin_time\": \"").append(recent_his[1]).append("\"}");
				
			}
			
			resp.append("]");
			
			if(num==0)
				
				resp = new StringBuffer();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}

	public String one_history(String hid) {

		StringBuffer resp = new StringBuffer();
		
//		StringBuffer sql = new StringBuffer("select * from history where id = '").append(hid).append("';");
		
		try {
			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			History h = historyrepository.findById(hid).get();
			
//			if(rs.next()) {
				
				resp.append("{ \"hid\": \"").append(h.getHistory_id()).append("\", ");
				
				resp.append("\"process\": \"").append(h.getHistory_process()).append("\", ");
				
				resp.append("\"begin_time\":\"").append(h.getHistory_begin_time()).append("\", ");
				
				resp.append("\"end_time\":\"").append(h.getHistory_end_time()).append("\", ");
				
				String processes = h.getHistory_input();
				
				String histories = h.getHistory_output();
				
				resp.append("\"input\":").append(list2JSON(processes)).append(", ");
				
				resp.append("\"output\":").append(list2JSON(histories)).append(" }");
				
//			}
			
		} catch (Exception e) {
		
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	// public static void main(String[] args) throws ParseException {
		
	// 	String jsonarray = "[{\"name\": \"1\"}, {\"name\": \"2\"}]";
		
	// 	JSONParser parser = new JSONParser();
		
	// 	JSONArray obj = (JSONArray)parser.parse(jsonarray);
		
	// 	System.out.println("parsed json objects: " + obj.size());
		
		
	// }

    public String getOwnerNameByID(String ownerid) {

		String ownername = "Public User";
		
		if(!bt.isNull(ownerid)) 
			ownername = ut.getUserById(ownerid).getUsername();

        return ownername;
    }

}
