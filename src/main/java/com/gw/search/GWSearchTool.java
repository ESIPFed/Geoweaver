package com.gw.search;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gw.database.HostRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.Host;
import com.gw.jpa.Workflow;

@Service
public class GWSearchTool {
	
	@Autowired
	HostRepository hostrepository;
	
	@Autowired
	ProcessRepository processrepository;
	
	@Autowired
	WorkflowRepository workflowrepository;
	
	private Logger logger = Logger.getLogger(GWSearchTool.class);
	
	public String turnResourceList2JSON(List<Resource> res) {
		
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

	public String search(String keywords, String type) {
		
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
	
	private List<Resource> searchHost(String keywords){
		
		List<Resource> res = new ArrayList();
		
		try {
		
			Collection<Host> hostlist = hostrepository.findHostsByNameAlike(keywords);
			
//			StringBuffer sql = new StringBuffer("select * from hosts where name like '%").append(keywords).append("%'; ");
//			
//			logger.debug(sql);
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			GWSearchTool tool = new GWSearchTool();
			
			Iterator<Host> it = hostlist.iterator();
			
			while(it.hasNext()) {
				
				Host h = it.next();
				
				Resource r = tool.new Resource();
				
				r.setId(h.getId());
				
				r.setName(h.getName());
				
				r.setDesc(h.getIp());
				
				r.setType("host");
				
				res.add(r);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return res;
		
	}
	
	private List<Resource> searchProcess(String keywords){
		
		List<Resource> res = new ArrayList();

		try {
		
//			StringBuffer sql = new StringBuffer("select * from process_type where name like '%").append(keywords).append("%'; ");
//			
//			logger.debug(sql);
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			Collection<GWProcess> processlist = processrepository.findProcessesByNameAlike(keywords);
			
			GWSearchTool tool = new GWSearchTool();
			
			Iterator<GWProcess> it = processlist.iterator();
			
			while(it.hasNext()) {
				
				Resource r = tool.new Resource();
				
				GWProcess p = it.next();
				
				r.setId(p.getId());
				
				r.setName(p.getName());
				
				r.setDesc(p.getDescription());
				
				r.setType("process");
				
				res.add(r);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return res;
		
	}
	
	private List<Resource> searchWorkflow(String keywords){
		
		List<Resource> res = new ArrayList();

		try {
		
//			StringBuffer sql = new StringBuffer("select * from abstract_model where name like '%").append(keywords).append("%'; ");
//			
//			logger.debug(sql);
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			Collection<Workflow> workflowlist = workflowrepository.findProcessesByNameAlike(keywords);
			
			GWSearchTool tool = new GWSearchTool();
			
			Iterator<Workflow> it = workflowlist.iterator();
			
			while(it.hasNext()) {
				
				Resource r = tool.new Resource();
				
				Workflow w = it.next();
				
				r.setId(w.getId());
				
				r.setName(w.getName());
				
				r.setDesc(w.getDescription());
				
				r.setType("workflow");
				
				res.add(r);
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return res;
		
	}
	
	/**
	 * This is the parent class of all host, process and workflow
	 */
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
