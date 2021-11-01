package com.gw.tools;

import java.io.File;
import java.nio.file.FileSystems;
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
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
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

	// @Autowired
	// UserTool ut;

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

			Optional<History> hisopt = historyrepository.findById(cid);
        	
        	History phis = hisopt.isPresent()? hisopt.get():null;
        	
        	// pt.stop(phis.getHistory_id());
			if(!bt.isNull(phis))
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

	public List<Workflow> getWorkflowListByOwner(String ownerid){

		Iterator<Workflow> wit = workflowrepository.findAllPublic().iterator();

		List<Workflow> actualList = new ArrayList<Workflow>();

		wit.forEachRemaining(actualList::add);

		wit = workflowrepository.findAllPrivateByOwner(ownerid).iterator();

		wit.forEachRemaining(actualList::add);

		return actualList;

	}
	
	public String list(String owner){
		
		// Iterator<Workflow> wit = workflowrepository.findAll().iterator();
		
		Iterator<Workflow> wit = workflowrepository.findAllPublic().iterator();
		
		StringBuffer json = new StringBuffer("[");
		
		while(wit.hasNext()) {
			
			Workflow w = wit.next();
			
			json.append(toJSON(w)).append(",");
			
		}

		wit = workflowrepository.findAllPrivateByOwner(owner).iterator();

		while(wit.hasNext()){

			json.append(toJSON(wit.next())).append(",");

		}

		json.deleteCharAt(json.length() - 1);
		
		json.append("]");
		
		return json.toString();
		
	}
	
	public Workflow getById(String id) {
		
		Optional<Workflow> wo = workflowrepository.findById(id);

		Workflow w = null;
		
		if(wo.isPresent())w = wo.get();
		
		return w;
		
	}
	
	public String detail(String id) {
		
		Optional<Workflow> wo = workflowrepository.findById(id);

		Workflow wf = null;
		
		if(wo.isPresent())wf = wo.get();
		
		return toJSON(wf);
		
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
	
	

	public List<Workflow> getAllWorkflow(){

		List<Workflow> wlist = new ArrayList();

		workflowrepository.findAll().forEach(w->wlist.add(w));

		return wlist;

	}

	public void save(Workflow w){

		Workflow wold = this.getById(w.getId());

		if(!bt.isNull(wold)){

			if(bt.isNull(w.getName())) w.setName(wold.getName());

			if(bt.isNull(w.getConfidential())) w.setConfidential(wold.getConfidential());

			if(bt.isNull(w.getDescription())) w.setDescription(wold.getDescription());

			if(bt.isNull(w.getEdges())) w.setEdges(wold.getEdges());

			if(bt.isNull(w.getNodes())) w.setNodes(wold.getNodes());

			if(bt.isNull(w.getOwner())) w.setOwner(wold.getOwner());

		}

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
	public String execute(String history_id, String wid, String mode, String[] hosts, String[] pswds, String[] envs, String token) {
		
		//use multiple threads to execute the processes
		
		String resp = null;
		
		try {
			
//			GeoweaverWorkflowTask task = new GeoweaverWorkflowTask("GW-Workflow-Run-" + token);
			
			// task.initialize(id, mode, hosts, pswds, token);
			
			// tm.addANewTask(task);

			task.initialize(history_id, wid, mode, hosts, pswds, envs, token);

			task.execute();

			resp = "{\"history_id\": \""+task.getHistory_id()+
					
					"\", \"token\": \""+token+
					
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
				
				resp.append("\"status\": \"").append(bt.escape(String.valueOf(hiscols[3]))).append("\", ");
				
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

	/**
	 * Download workflow 
	 * @param wid
	 * workflow id
	 * @param option
	 * workflowonly | workflowwithprocesscode | workflowwithprocesscodehistory
	 * @return
	 * @throws ParseException
	 */
    public String download(String wid, String option) throws ParseException {

		Workflow wf = this.getById(wid);

		String fileurl = "download/temp/" + wf.getId() + ".zip";

		String savefilepath = bt.getFileTransferFolder() + wf.getId() + FileSystems.getDefault().getSeparator();
		
		File tf = new File(savefilepath);

		bt.deleteDirectory(tf);
		
		if(!tf.exists()) tf.mkdirs();

		String workflowstring = bt.toJSON(wf);

		bt.writeString2File(workflowstring, savefilepath + "workflow.json");

		if("workflowwithprocesscode".equals(option) || "workflowwithprocesscodehistory".equals(option)){

			JSONParser jsonParser=new JSONParser();

			JSONArray arrayobj=(JSONArray) jsonParser.parse(wf.getNodes());

			String codesavefile = savefilepath + "code" + FileSystems.getDefault().getSeparator();

			File codef = new File(codesavefile);
		
			if(!codef.exists()) codef.mkdirs();

			for (int i = 0; i < arrayobj.size(); i++)
			{

				JSONObject jsonObj = (JSONObject) arrayobj.get(i);

				String process_workflow_id = (String)jsonObj.get("id");

				String process_name = (String)jsonObj.get("title");

				String process_id = process_workflow_id.split("-")[0];

				String targetsourcefile = codesavefile + pt.getProcessFileName(process_id);

				if(new File(targetsourcefile).exists()) continue;

				bt.writeString2File(pt.getProcessById(process_id).getCode(), targetsourcefile);

			}

		}
		
		if("workflowwithprocesscodehistory".equals(option)){

			String wfhistorysavefile = savefilepath + "history" + FileSystems.getDefault().getSeparator() + wid + ".json";

			//first save workflow history

			List<History> histlist = historyrepository.findByWorkflowId(wid);

			StringBuffer workflowhistory = new StringBuffer("[");

			String prefix = "";
			
			for(History h: histlist){
			
				String historystr = bt.toJSON(h);
			
				workflowhistory.append(prefix);
  			
				prefix = ","; 
			
				workflowhistory.append(historystr); 
			
			};

			workflowhistory.append("]");

			bt.writeString2File(workflowhistory.toString(), wfhistorysavefile);
			
			//second, save process history of one workflow execution into a file
			for(History h : histlist){

				String[] processhistorylist = h.getHistory_output().split(";");

				prefix = "";

				String processhistorysavefile = savefilepath + "history" + FileSystems.getDefault().getSeparator() + h.getHistory_id() + ".json";

				StringBuffer processhistorybuffer = new StringBuffer("[");

				for(String processhitoryid: processhistorylist){

					Optional<History> hisop = historyrepository.findById(processhitoryid);

					if(hisop.isPresent()){

						processhistorybuffer.append(prefix);
  			
						prefix = ","; 
	
						processhistorybuffer.append(bt.toJSON(hisop.get())); 

					}

				}

				processhistorybuffer.append("]");

				bt.writeString2File(processhistorybuffer.toString(), processhistorysavefile);

			}

		}
		
		bt.zipFolder(savefilepath, bt.getFileTransferFolder() + wf.getId() + ".zip");

        return fileurl;
    }

    public String precheck(String filename) {

		// { "url": "download/temp/aavthwdfvxinra0a0rsw.zip", "filename": "aavthwdfvxinra0a0rsw.zip" }

		String filepath = bt.getFileTransferFolder() + filename;

		if(filename.endsWith(".zip")){
			
			bt.unzip(filepath, bt.getFileTransferFolder());
		}


        return null;

    }
	
	// public static void main(String[] args) throws ParseException {
		
	// 	String jsonarray = "[{\"name\": \"1\"}, {\"name\": \"2\"}]";
		
	// 	JSONParser parser = new JSONParser();
		
	// 	JSONArray obj = (JSONArray)parser.parse(jsonarray);
		
	// 	System.out.println("parsed json objects: " + obj.size());
		
		
	// }

    // public String getOwnerNameByID(String ownerid) {

	// 	String ownername = "Public User";
		
	// 	if(!bt.isNull(ownerid)) 
	// 		ownername = ut.getUserById(ownerid).getUsername();

    //     return ownername;
    // }

}
