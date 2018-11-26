package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.GeoweaverWorkflowTask;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.TaskManager;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.STATUS;
import edu.gmu.csiss.earthcube.cyberconnector.web.GeoweaverController;

/**
 * 
 * @author JensenSun
 *
 */
public class WorkflowTool {
	
	public static String list(String owner){
		
		StringBuffer json = new StringBuffer("[");
		
		try {
			
			ResultSet rs = DataBaseOperation.query("select * from abstract_model where length(identifier) < 30; ");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					json.append(",");
					
				}
				
				json.append("{ \"id\": \"")
					.append(rs.getString("identifier"))
					.append("\", \"name\": \"")
					.append(rs.getString("name"))
					.append("\" }");
				
				num++;
				
			}
			
			json.append("]");
			
		} catch (SQLException e) {

			e.printStackTrace();
			
		}finally {

			DataBaseOperation.closeConnection();
			
		}
		
		return json.toString();
		
	}
	
	public static Workflow getById(String id) {
		
		Workflow w = null;

		StringBuffer sql = new StringBuffer("select * from abstract_model where identifier = \"").append(id).append("\";");
		
		StringBuffer resp = new StringBuffer();
		
		try {
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			if(rs.next()) {
				
				w = new Workflow();
				
				w.setName(rs.getString("name"));
				
				w.setId(rs.getString("id"));
				
				w.setNodes(rs.getString("process_connection"));
				
				w.setEdges(rs.getString("param_connection"));
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
		
		return w;
		
	}

	public static String detail(String id) {
		
		StringBuffer sql = new StringBuffer("select * from abstract_model where identifier = \"").append(id).append("\";");
		
		StringBuffer resp = new StringBuffer();
		
		try {
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			if(rs.next()) {
				
				resp.append("{ \"name\":\"");
				
				resp.append(rs.getString("name")).append("\", \"id\": \"");
				
				resp.append(id).append("\", \"nodes\":");
				
				resp.append(rs.getString("process_connection")).append(", \"edges\":");
				
				resp.append(rs.getString("param_connection")).append(" }");
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
		
		return resp.toString();
		
	}
	
	public static Map<String, List> getNodeConditionMap(JSONArray nodes, JSONArray edges) throws ParseException{
		
		//find the condition nodes of each process
		
		Map<String, List> node2condition = new HashMap();
		
		for(int i=0;i<nodes.size();i++) {
			
			String current_id = (String)((JSONObject)nodes.get(i)).get("id");
			
			List preids = new ArrayList();
			
			for(int j=0;j<edges.size();j++) {
				
				JSONObject eobj = (JSONObject)edges.get(j);
				
				String sourceid = (String)((JSONObject)eobj.get("source")).get("id");
				
				String targetid = (String)((JSONObject)eobj.get("target")).get("id");
				
				if(current_id.equals(targetid)) {
					
					preids.add(sourceid);
					
				}
				
				
			}

			node2condition.put(current_id, preids);
			
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
	public static String[] findNextProcess(Map<String, List> nodemap, STATUS[] flags, JSONArray nodes) {
		
		String id = null;
		
		String num = null;
		
		for(int i=0;i<nodes.size();i++) {
			
			String currentid = (String)((JSONObject)nodes.get(i)).get("id");
			
			if(checkNodeStatus(currentid, flags, nodes)!=STATUS.READY) {
				
				continue;
				
			}
			
			List prenodes = nodemap.get(currentid);
			
			boolean satisfied = true;
			
			//check if all the prenodes are satisfied
			
			for(int j=0;j<prenodes.size();j++) {
				
				String prenodeid = (String)prenodes.get(j);
				
				//if any of the pre- nodes is not satisfied, this node is passed. 
				
				if(checkNodeStatus(prenodeid, flags, nodes)!=STATUS.DONE
						&&checkNodeStatus(prenodeid, flags, nodes)!=STATUS.FAILED) {
					
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
	public static void updateNodeStatus(String id, STATUS[] flags, JSONArray nodes, STATUS status) {
		
		for(int j=0;j<nodes.size();j++) {
			
			String prenodeid = (String)((JSONObject)nodes.get(j)).get("id");
			
			if(prenodeid.equals(id)) {
				
				flags[j] = status;
				
				break;
				
			}
			
		}
		
	}
	
	/**
	 * Check the status of a node
	 * @param id
	 * @param flags
	 * @param nodes
	 * @return
	 */
	private static STATUS checkNodeStatus(String id, STATUS[] flags, JSONArray nodes) {
		
		STATUS status = null;
		
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
	public static String execute(String id, String mode, String[] hosts, String[] pswds, String token) {
		
		//use multiple threads to execute the processes
		
		String resp = null;
		
		try {
			
			GeoweaverWorkflowTask task = new GeoweaverWorkflowTask(id);
			
			if(BaseTool.isNull(token)) {
				
				token = new RandomString(12).nextString(); //this token will be used to establish websocket session
				
			}
			
			task.initialize(id, mode, hosts, pswds, token);
			
			TaskManager.addANewTask(task);

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
	public static void update(String wid, String nodes, String edges) {
		
		StringBuffer sql = new StringBuffer("update abstract_model set process_connection = ?, param_connection = ? where identifier = \"");
		
		sql.append(wid).append("\"; ");
		
		DataBaseOperation.preexecute(sql.toString(), new String[] {nodes, edges});
		
		
	}

	
	public static String add(String name, String nodes, String edges) {
		
		String newid = new RandomString(20).nextString();
		
		StringBuffer sql = new StringBuffer("insert into abstract_model (identifier, name, namespace, process_connection, param_connection) values (\"");
		
		sql.append(newid).append("\", \"");
		
		sql.append(name).append("\", \"http://geoweaver.csiss.gmu.edu/workflow/");
		
		sql.append(name).append("\", ?, ? )");
		
		DataBaseOperation.preexecute(sql.toString(), new String[] {nodes, edges});
		
		return newid;
		
	}
	
	public static String del(String workflowid) {
		
		StringBuffer sql = new StringBuffer("delete from abstract_model where identifier = '").append(workflowid).append("';");
		
		DataBaseOperation.execute(sql.toString());
		
		return "done";
		
	}

	/**
	 * show the history of every execution of the workflow
	 * @param string
	 * @return
	 */
	public static String all_history(String workflow_id) {
		

		StringBuffer resp = new StringBuffer() ;
		
		StringBuffer sql = new StringBuffer("select * from history where process = \"").append(workflow_id).append("\";");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"begin_time\": \"").append(rs.getString("begin_time")).append("\"}");
				
				num++;
				
			}
			
			resp.append("]");
			
			if(num==0)
				
				resp = new StringBuffer();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	/**
	 * List to JSON
	 * @param list
	 * @return
	 */
	public static String list2JSON(String list) {
		
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

	public static String one_history(String hid) {

		StringBuffer resp = new StringBuffer();
		
		StringBuffer sql = new StringBuffer("select * from history where id = \"").append(hid).append("\";");
		
		try {
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			if(rs.next()) {
				
				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"process\": \"").append(rs.getString("process")).append("\", ");
				
				resp.append("\"begin_time\":\"").append(rs.getString("begin_time")).append("\", ");
				
				resp.append("\"end_time\":\"").append(rs.getString("end_time")).append("\", ");
				
				String processes = rs.getString("input");
				
				String histories = rs.getString("output");
				
				resp.append("\"input\":").append(list2JSON(processes)).append(", ");
				
				resp.append("\"output\":").append(list2JSON(histories)).append(" }");
				
			}
			
		} catch (SQLException e) {
		
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	public static void main(String[] args) throws ParseException {
		
		String jsonarray = "[{\"name\": \"1\"}, {\"name\": \"2\"}]";
		
		JSONParser parser = new JSONParser();
		
		JSONArray obj = (JSONArray)parser.parse(jsonarray);
		
		System.out.println("parsed json objects: " + obj.size());
		
		
	}

}
