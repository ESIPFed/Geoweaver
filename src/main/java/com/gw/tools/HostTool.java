package com.gw.tools;


import java.rmi.Remote;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.History;
import com.gw.jpa.Host;
import com.gw.local.LocalSession;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class HostTool {

	Logger logger = Logger.getLogger(HostTool.class);
	
	@Autowired
	HostRepository hostrepository;
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Autowired
	BaseTool bt;

	@Autowired
	EnvironmentRepository environmentrepository;
	
	
	
	
	/**
	 * Get History by ID
	 * @param hid
	 * @return
	 */
	public String one_history(String hid) {
		
		StringBuffer resp = new StringBuffer();
		
		try {
			
			History his = historyrepository.findById(hid).get();
			
				resp.append("{ \"hid\": \"").append(his.getHistory_id()).append("\", ");
				
				resp.append("\"id\": \"").append(his.getHistory_id()).append("\", ");
				
				resp.append("\"process\": \"").append(his.getHistory_process()).append("\", ");
				
				resp.append("\"name\": \"").append(his.getHistory_input()).append("\", ");
				
				resp.append("\"begin_time\":\"").append(his.getHistory_begin_time()).append("\", ");
				
				resp.append("\"end_time\":\"").append(his.getHistory_end_time()).append("\", ");
				
				resp.append("\"input\":\"").append(his.getHistory_input()).append("\", ");
				
				resp.append("\"output\":\"").append(bt.escape(his.getHistory_output())).append("\", ");
				
				resp.append("\"host\":\"").append(his.getHost_id()).append("\", ");
				
				resp.append("\"status\":\"").append(his.getIndicator()).append("\" }");
				
			
		} catch (Exception e) {
		
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	
	
	public String recent(String hostid, int limit) {
		
		StringBuffer resp = new StringBuffer();
		
		Collection<History> historylist = historyrepository.findRecentHistory(hostid, limit);
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			Iterator<History> hisint = historylist.iterator();
			
			while(hisint.hasNext()) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				History h = hisint.next();

				resp.append("{ \"id\": \"").append(h.getHistory_id()).append("\", ");
				
				resp.append("\"name\": \"").append(h.getHistory_process()).append("\", ");
				
				resp.append("\"end_time\": \"").append(h.getHistory_end_time()).append("\", ");

				resp.append("\"notes\": \"").append(h.getHistory_notes()).append("\", ");
				
				resp.append("\"status\": \"").append(h.getIndicator()).append("\", ");
				
				resp.append("\"begin_time\": \"").append(h.getHistory_begin_time()).append("\"}");
				
				num++;
				
			}
			
			resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	/**
	 * Detail JSON object
	 * @param id
	 * @return
	 */
	public String detailJSONObj(String id) {
		
		Host h = hostrepository.findById(id).get();
		
		String json = toJSON(h);
		
		return json;
		
	}
	
	public String detail(String id) {
		
		String detail = detailJSONObj(id);
		
		return detail;
		
	}
	
	public Host getHostById(String id) {
		
		Optional<Host> oh = hostrepository.findById(id);


		Host h = oh.isPresent()?oh.get():null;
		
		return h;
	}
	
	public String[] getHostDetailsById(String id) {
		
		String[] hostdetails = new String[6] ;
		
		try {
			
			Host h = hostrepository.findById(id).get();
				
			hostdetails[0] = h.getName();
			
			hostdetails[1] = h.getIp();
			
			hostdetails[2] = h.getPort();
			
			hostdetails[3] = h.getUsername();
			
			hostdetails[4] = h.getType();
			
			hostdetails[5] = h.getUrl();
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return hostdetails;
	}
	
	public String toJSON(Host h) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(h);
            // logger.debug("ResultingJSONstring = " + json);
            //System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}
	
	
	
	// public String listWithEnvironments(String owner){

	// 	Iterator<Host> hostit = hostrepository.findAllPublicHosts().iterator();
		
	// 	List<Host> hostlist = new ArrayList();

	// 	hostit.forEachRemaining(hostlist::add);

	// 	hostit = hostrepository.findPrivateByOwner(owner).iterator();

	// 	hostit.forEachRemaining(hostlist::add);

		
		
		
	// }

	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public String list(String owner) {
		
		StringBuffer json = new StringBuffer("[");

		List<Host> hostlist = new ArrayList();
		
		Iterator<Host> hostit = hostrepository.findAllPublicHosts().iterator();

		hostit.forEachRemaining(hostlist::add);
		
		hostit = hostrepository.findPrivateByOwner(owner).iterator();

		hostit.forEachRemaining(hostlist::add);

		hostlist.forEach((host)->{json.append(toJSON(host)).append(",");});

		if(json.length()>1) json.deleteCharAt(json.length() - 1);
		
		json.append("]");
		
		return json.toString();
		
	}
	
	
	/**
	 * Add a new host
	 * @param hostname
	 * @param hostip
	 * @param hostport
	 * @param username
	 * @param owner
	 */
	public String add(String hostname, String hostip, String hostport, String username, String url, String type, String owner, String confidential) {
		
		String newhostid = new RandomString(6).nextString();
		
		Host h = new Host();

		if(BaseTool.isNull(owner)) owner = "111111"; //default to be the public user
		
		h.setId(newhostid);
		h.setIp(hostip);
		h.setName(hostname);
		h.setOwner(owner);
		h.setPort(hostport);
		h.setType(type);
		h.setUrl(url);
		h.setUsername(username);
		h.setConfidential(confidential);
		
		hostrepository.save(h);
		
		return newhostid;
		
	}
	
	/**
	 * Remove a host from database
	 * @param hostid
	 */
	public String del(String hostid) {
		
		hostrepository.deleteById(hostid);
		
		return "done";
		
	}

	public void save(Host h){

		hostrepository.save(h);

	}

	public List<Host> getAllHosts(){

		List<Host> hostlist = new ArrayList();

		hostrepository.findAll().forEach(h->hostlist.add(h));

		return hostlist;

	}

	public void saveEnvironment(Environment newenv){
		environmentrepository.save(newenv);
	}

	
	

	/**
	 * Update the Host table
	 * @param hostname
	 * @param hostip
	 * @param hostport
	 * @param username
	 * @param type
	 * @param object
	 * @return
	 */
	public String update(String hostid, String hostname, String hostip, String hostport, String username, String type, String owner, String url, String confidential) {

		String resp = null;
		
		try {
			
			Host h = this.getHostById(hostid);
			
			// h.setId(hostid);
			
			h.setName(hostname);
			
			if(!BaseTool.isNull(hostip)) h.setIp(hostip);
			
			if(!BaseTool.isNull(hostport)) h.setPort(hostport);
			
			if(!BaseTool.isNull(username)) h.setUsername(username);
			
			if(!BaseTool.isNull(type)) h.setType(type);
			
			
			if(!BaseTool.isNull(owner)) h.setOwner(owner);
			
			if(!BaseTool.isNull(url)) h.setUrl(url);

			if(!BaseTool.isNull(confidential)) h.setConfidential(confidential);
			
			hostrepository.save(h);
			
		} catch (Exception e) {

			e.printStackTrace();
			
			logger.error("Failed to update the host table " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	
	

}
