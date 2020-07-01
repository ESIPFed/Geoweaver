package gw.search;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import gw.database.DataBaseOperation;

public class GWSearchTool {
	
	private final static Logger logger = Logger.getLogger(GWSearchTool.class);
	
	public static String turnResourceList2JSON(List<Resource> res) {
		
		StringBuffer json = new StringBuffer("[");
		
		for(int i=0;i<res.size();i++) {
			
			if(i!=0) json.append(", ");
			
			json.append("{ \"id\": \"").append(res.get(i).getId())
				.append("\", \"name\": \"").append(res.get(i).getName())
				.append("\", \"type\": \"").append(res.get(i).getType())
				.append("\", \"desc\": \"").append(res.get(i).getDesc())
				.append("\" }");
			
		}
		
		json.append("]");
		
		return json.toString();
		
	}

	public static String search(String keywords, String type) {
		
		StringBuffer resp = new StringBuffer();
		
		try {

			type = type.toLowerCase();
			
			if("all".equals(type)) {
				
				List<Resource> hosts = searchHost(keywords);
				
				List<Resource> processes = searchProcess(keywords);
				
				List<Resource> workflows = searchWorkflow(keywords);
				
				hosts.addAll(processes);
				
				hosts.addAll(workflows);
				
				resp.append(turnResourceList2JSON(hosts));
				
			}else if("host".equals(type)) {
				
				List<Resource> hosts = searchHost(keywords);
				
				resp.append(turnResourceList2JSON(hosts));
				
			}else if("process".equals(type)) {
				
				List<Resource> processes = searchProcess(keywords);
				
				resp.append(turnResourceList2JSON(processes));
				
			}else if("workflow".equals(type)) {
				
				List<Resource> workflows = searchWorkflow(keywords);
				
				resp.append(turnResourceList2JSON(workflows));
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	private static List<Resource> searchHost(String keywords){
		
		List<Resource> res = new ArrayList();
		
		try {
		
			StringBuffer sql = new StringBuffer("select * from hosts where name like '%").append(keywords).append("%'; ");
			
			logger.debug(sql);
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			GWSearchTool tool = new GWSearchTool();
			
			while(rs.next()) {
				
				Resource r = tool.new Resource();
				
				r.setId(rs.getString("id"));
				
				r.setName(rs.getString("name"));
				
				r.setDesc(rs.getString("ip"));
				
				r.setType("host");
				
				res.add(r);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return res;
		
	}
	
	private static List<Resource> searchProcess(String keywords){
		
		List<Resource> res = new ArrayList();

		try {
		
			StringBuffer sql = new StringBuffer("select * from process_type where name like '%").append(keywords).append("%'; ");
			
			logger.debug(sql);
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			GWSearchTool tool = new GWSearchTool();
			
			while(rs.next()) {
				
				Resource r = tool.new Resource();
				
				r.setId(rs.getString("id"));
				
				r.setName(rs.getString("name"));
				
				r.setDesc(rs.getString("description"));
				
				r.setType("process");
				
				res.add(r);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return res;
		
	}
	
	private static List<Resource> searchWorkflow(String keywords){
		
		List<Resource> res = new ArrayList();

		try {
		
			StringBuffer sql = new StringBuffer("select * from abstract_model where name like '%").append(keywords).append("%'; ");
			
			logger.debug(sql);
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			GWSearchTool tool = new GWSearchTool();
			
			while(rs.next()) {
				
				Resource r = tool.new Resource();
				
				r.setId(rs.getString("identifier"));
				
				r.setName(rs.getString("name"));
				
				r.setDesc(rs.getString("description"));
				
				r.setType("workflow");
				
				res.add(r);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return res;
		
	}
	
	private class Resource{
		
		String id, name, type, desc;
		
		

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getId() {
			return id;
		}

		public void setId(String id) {
			this.id = id;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
		
	}
	
	
	
}
