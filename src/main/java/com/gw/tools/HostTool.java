package com.gw.tools;


import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.History;
import com.gw.jpa.Host;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HostTool {

	Logger logger = Logger.getLogger(HostTool.class);
	
	@Autowired
	HostRepository hostrepository;
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	EnvironmentRepository environmentrepository;
	
	
	/**
	 * Judge if the host is localhost
	 * @param hid
	 * @return
	 */
	public boolean islocal(String hid) {
		
		boolean is = false;
		
		Host h = hostrepository.findById(hid).get();
		
		if("127.0.0.1".equals(h.getIp()) || "localhost".equals(h.getIp())) {
			
			is = true;
			
		}
		
		return is;
		
	}
	
	/**
	 * Get History by ID
	 * @param hid
	 * @return
	 */
	public String one_history(String hid) {
		
		StringBuffer resp = new StringBuffer();
		
//		StringBuffer sql = new StringBuffer("select * from history where history.id = '").append(hid).append("';");
		
//		logger.info(sql.toString());
		
		try {
			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			History his = historyrepository.findById(hid).get();
			
				resp.append("{ \"hid\": \"").append(his.getHistory_id()).append("\", ");
				
				resp.append("\"id\": \"").append(his.getHistory_id()).append("\", ");
				
				resp.append("\"process\": \"").append(his.getHistory_process()).append("\", ");
				
				resp.append("\"name\": \"").append(his.getHistory_input()).append("\", ");
				
				resp.append("\"begin_time\":\"").append(his.getHistory_begin_time()).append("\", ");
				
				resp.append("\"end_time\":\"").append(his.getHistory_end_time()).append("\", ");
				
				resp.append("\"input\":\"").append(his.getHistory_input()).append("\", ");
				
				resp.append("\"output\":\"").append(pt.escape(his.getHistory_output())).append("\", ");
				
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
		
		
		
//		StringBuffer sql = new StringBuffer("select * from history where host = '")
//				.append(hostid).append("' ORDER BY begin_time DESC limit ").append(limit).append(";");
		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
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
		
//		String sql = "select * from hosts where id = '" + id + "';";
//		
//		ResultSet rsmd = DataBaseOperation.query(sql);
//
//	    JSONObject obj = new JSONObject();
//	      
//		try {
//			
//			if(rsmd.next()) {
//				
//			      int numColumns = rsmd.getMetaData().getColumnCount();
//			      
//			      for (int i=1; i<numColumns+1; i++) {
//			      
//			    	String column_name = rsmd.getMetaData().getColumnName(i).toLowerCase();
//
//			        if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.ARRAY){
//			        	obj.put(column_name, rsmd.getArray(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BIGINT){
//			        	obj.put(column_name, rsmd.getInt(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BOOLEAN){
//			        	obj.put(column_name, rsmd.getBoolean(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.BLOB){
//			        	obj.put(column_name, rsmd.getBlob(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.DOUBLE){
//			        	obj.put(column_name, rsmd.getDouble(column_name)); 
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.FLOAT){
//			        	obj.put(column_name, rsmd.getFloat(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.INTEGER){
//			        	obj.put(column_name, rsmd.getInt(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.NVARCHAR){
//			        	obj.put(column_name, rsmd.getNString(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.VARCHAR){
//			        	obj.put(column_name, rsmd.getString(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.TINYINT){
//			        	obj.put(column_name, rsmd.getInt(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.SMALLINT){
//			        	obj.put(column_name, rsmd.getInt(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.DATE){
//			        	obj.put(column_name, rsmd.getDate(column_name));
//			        }
//			        else if(rsmd.getMetaData().getColumnType(i)==java.sql.Types.TIMESTAMP){
//			        	obj.put(column_name, rsmd.getTimestamp(column_name));   
//			        }
//			        else{
//			        	obj.put(column_name, rsmd.getObject(column_name));
//			        }
//			      }
//
//			}
//			
//			
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//			
//		}finally {
//			
//			DataBaseOperation.closeConnection();
//			
//		}
//
//		
//		return obj;
		
	}
	
	public String detail(String id) {
		
		String detail = detailJSONObj(id);
		
		return detail;
		
	}
	
	public Host getHostById(String id) {
		
		Host h = hostrepository.findById(id).get();
		
//		Host h = new Host();
//		
//		StringBuffer sql = new StringBuffer("select * from hosts where id = '").append(id).append("'; ");
//		
//		String[] hostdetails = new String[6] ;
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		try {
//			
//			if(rs.next()) {
//				
//				h.setName(rs.getString("name"));;
//				
//				h.setIp(rs.getString("ip"));
//				
//				h.setPort(rs.getString("port"));
//				
//				h.setUsername(rs.getString("username"));
//				
//				h.setType(rs.getString("type"));
//				
//				h.setUrl(rs.getString("url"));
//				
//				h.setId(id);
//				
//				h.setOwner(rs.getString("owner"));
//				
//			}
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}
		
		return h;
	}
	
	public String[] getHostDetailsById(String id) {
		
//		StringBuffer sql = new StringBuffer("select * from hosts where id = '").append(id).append("'; ");
		
		String[] hostdetails = new String[6] ;
		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
//			if(rs.next()) {
			
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
            json = mapper.writeValueAsString(h);
            logger.debug("ResultingJSONstring = " + json);
            //System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}
	
	public String toJSON(Environment env) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(env);
            logger.debug("ResultingJSONstring = " + json);
            //System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}
	
	/**
	 * 
	 * @return
	 * @throws SQLException 
	 */
	public String list(String owner) {
		
//		List<Host> hostlist =  hostrepository.findByOwner(owner);
		
		Iterator<Host> hostit = hostrepository.findAll().iterator();
		
		StringBuffer json = new StringBuffer("[");
		
		int num = 0;
		
		while(hostit.hasNext()) {
			
			Host h = hostit.next();
			
			if( num++ != 0) {
				
				json.append(",");
				
			}
			
			json.append(toJSON(h));
			
		}
		
		json.append("]");
		
		return json.toString();
		
//		StringBuffer json = new StringBuffer("[");
//		try {
//			
//			String sql = "select id, ip, url, type, port, name from hosts;";
//			
//			ResultSet rs = DataBaseOperation.query(sql);
//			
//			int num = 0;
//			
//			while(rs.next()) {
//				
//				String hostid = rs.getString("id");
//				
//				String hostname = rs.getString("name");
//				
//				String ip = rs.getString("ip");
//				
//				String url = rs.getString("url");
//				
//				String port = rs.getString("port");
//				
//				String type = rs.getString("type");
//				
//				if( num++ != 0) {
//					
//					json.append(",");
//					
//				}
//				
//				json.append("{\"id\":\"").append(hostid)
//					.append("\", \"name\": \"").append(hostname)
//					.append("\", \"ip\": \"").append(ip)
//					.append("\", \"url\": \"").append(url)
//					.append("\", \"port\": \"").append(port)
//					.append("\", \"type\": \"").append(type)
//					.append("\"}");
//				
//			}
//			
//		}catch(Exception e) {
//			
//			e.printStackTrace();
//			
//		}finally {
//			
//			DataBaseOperation.closeConnection();
//			
//		}
//		
//		json.append("]");
//		
//		return json.toString();
		
	}
	
	
	/**
	 * Add a new host
	 * @param hostname
	 * @param hostip
	 * @param hostport
	 * @param username
	 * @param owner
	 */
	public String add(String hostname, String hostip, String hostport, String username, String url, String type, String owner) {
		
		String newhostid = new RandomString(6).nextString();
		
		Host h = new Host();
		
		h.setId(newhostid);
		h.setIp(hostip);
		h.setName(hostname);
		h.setOwner(owner);
		h.setPort(hostport);
		h.setType(type);
		h.setUrl(url);
		h.setUsername(username);
		
		hostrepository.save(h);
		
//		StringBuffer sql = new StringBuffer("insert into hosts (id, name, ip, port, url, type, username, owner) values ('")
//				
//				.append(newhostid).append("', '")
//				
//				.append(hostname).append("', '")
//				
//				.append(hostip).append("', '")
//				
//				.append(hostport).append("', '")
//				
//				.append(url).append("', '")
//				
//				.append(type).append("', '")
//				
//				.append(username).append("', '")
//				
//				.append(owner).append("'); ");
//		
//		DataBaseOperation.execute(sql.toString());
		
		return newhostid;
		
	}
	
	/**
	 * Remove a host from database
	 * @param hostid
	 */
	public String del(String hostid) {
		
//		StringBuffer sql = new StringBuffer("delete from hosts where id = '").append(hostid).append("';");
//		
//		DataBaseOperation.execute(sql.toString());
		
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

	/**
	 * Add environment to database
	 * @param historyid
	 * @param bin
	 * @param env
	 * @param basedir
	 * @return
	 */
	public String addEnv(String historyid, String hostid, String type, String bin, String env, String basedir, String settings) {
		
		String resp = null;
		
		try {
			
//			String enviroment = getEnvironmentByBEB(hostid, bin, env, basedir);

			if(!bt.isNull(bin) && !bt.isNull(env) && !bt.isNull(basedir) && !bt.isNull(settings)){
				
				Iterator<Environment> eit = environmentrepository.findEnvByID_BIN_ENV_BaseDir(hostid, bin, env, basedir).iterator();
				
				Environment newenv = new Environment();
				
				if(eit.hasNext()) {
					
					newenv = eit.next();
					
				}else {
					
					newenv.setId(historyid);
					
					newenv.setName(bin+"-"+env+"-"+basedir);
					
					newenv.setType(type);
					
					newenv.setBin(bin);
					
					newenv.setPyenv(env);
					
					newenv.setBasedir(basedir);
					
					newenv.setSettings(settings);
					
					newenv.setHost(hostid);
					
					environmentrepository.save(newenv);
					
				}

			}else{

				logger.debug("one of the bin, env, basedir, settings is null and the environment will not be saved into database.");

			}
			
			
			
			
//			logger.info("existing environment " + enviroment);
//			
//			if(enviroment.equals("[]")) {
//				
//				StringBuffer sql = new StringBuffer("insert into environment (id, name, type, bin, pyenv, host, basedir, settings) values ('");
//				
//				sql.append(historyid).append("', '");
//				
//				sql.append(bin).append("-").append(env).append("-").append(basedir).append("', '");
//				
//				sql.append(type).append("', '");
//				
//				sql.append(bin).append("', '");
//				
//				sql.append(env).append("', '");
//
//				sql.append(hostid).append("', '");
//				
//				sql.append(basedir).append("', '");
//				
//				sql.append(settings).append("' ); ");
//				
//				logger.info(sql);
//				
//				DataBaseOperation.execute(sql.toString());
//				
//			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp;
		
	}
	
	public void showAllEnvironment() {
		

		String resp = null;
		
		try {
			
//			StringBuffer sql = new StringBuffer("select * from environment ;");
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			Iterator<Environment> envit = environmentrepository.findAll().iterator();
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			while(envit.hasNext()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				Environment newenv = envit.next();
				
				envstr.append(toJSON(newenv));
				
//				envstr.append("{ \"id\": \"").append(newenv.getId());
//				
//				envstr.append("\", \"name\": \"").append(rs.getString("name"));
//				
//				envstr.append("\", \"type\": \"").append(rs.getString("type"));
//				
//				envstr.append("\", \"bin\": \"").append(rs.getString("bin"));
//				
//				envstr.append("\", \"pyenv\": \"").append(rs.getString("pyenv")).append("\" }");
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
			logger.debug(resp);
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
	}
	
	public String getEnvironmentByBEB(String hostid, String bin, String env, String basedir) {
		
		String resp = null;
		
		try {
			
//			StringBuffer sql = new StringBuffer("select * from environment where host = '").append(hostid)
//					.append("' and bin = '").append(bin).append("' and pyenv = '")
//					.append(env).append("' and basedir = '").append(basedir).append("';");
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			Collection<Environment> envlist = environmentrepository.findEnvByID_BIN_ENV_BaseDir(hostid, bin, env, basedir);
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			Iterator<Environment> it = envlist.iterator();
			
			while(it.hasNext()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				Environment newenv = it.next();
				
				envstr.append(toJSON(newenv));
				
//				envstr.append("{ \"id\": \"").append(rs.getString("id"));
//				
//				envstr.append("\", \"name\": \"").append(rs.getString("name"));
//				
//				envstr.append("\", \"type\": \"").append(rs.getString("type"));
//				
//				envstr.append("\", \"bin\": \"").append(rs.getString("bin"));
//				
//				envstr.append("\", \"basedir\": \"").append(rs.getString("basedir"));
//				
//				envstr.append("\", \"pyenv\": \"").append(rs.getString("pyenv")).append("\" }");
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return resp;
		
	}
	
	/**
	 * Get environments by host
	 * @param hid
	 * @return
	 */
	public String getEnvironments(String hid) {
		
		String resp = null;
		
		try {
			
//			StringBuffer sql = new StringBuffer("select * from environment where host = '").append(hid).append("';");
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			Collection<Environment> envlist = environmentrepository.findEnvByHost(hid);
			
			StringBuffer envstr = new StringBuffer();
			
			envstr.append("[");
			
			int num = 0;
			
			Iterator<Environment> it = envlist.iterator();
			
			while(it.hasNext()) {
				
				if(num!=0) {
					
					envstr.append(", ");
					
				}
				
				Environment newenv = it.next();
				
				envstr.append(toJSON(newenv));
				
//				envstr.append("{ \"id\": \"").append(rs.getString("id"));
//				
//				envstr.append("\", \"name\": \"").append(rs.getString("name"));
//				
//				envstr.append("\", \"type\": \"").append(rs.getString("type"));
//				
//				envstr.append("\", \"bin\": \"").append(rs.getString("bin"));
//				
//				envstr.append("\", \"basedir\": \"").append(rs.getString("basedir"));
//				
//				envstr.append("\", \"pyenv\": \"").append(rs.getString("pyenv")).append("\" }");
				
				num++;
				
			}
			
			envstr.append("]");
			
			resp = envstr.toString();
			
			logger.debug("the python environment for host: " + hid + " " + resp);
			
		} catch (Exception e) {

			e.printStackTrace();
			
		}
		
		return resp;
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
	public String update(String hostid, String hostname, String hostip, String hostport, String username, String type, String owner, String url) {

		String resp = null;
		
		try {
			
			Host h = new Host();
			
			h.setId(hostid);
			
			h.setName(hostname);
			
			if(!bt.isNull(hostip)) h.setIp(hostip);
			
			if(!bt.isNull(hostport)) h.setPort(hostport);
			
			h.setUsername(username);
			
			h.setType(type);
			
			h.setOwner(owner);
			
			h.setUrl(url);
			
			hostrepository.save(h);
			
//			StringBuffer sql = new StringBuffer("update hosts set ")
//					.append("name='").append(hostname).append("', ");
//			
//			if(!bt.isNull(hostip))
//					sql.append("ip='").append(hostip).append("', ");
//			
//			if(!bt.isNull(hostport))
//					sql.append("port=").append(hostport).append(", ");
//			
//					
//			sql.append("username='").append(username).append("', ")
//					.append("owner='").append(owner).append("', ")
//					.append("type='").append(type).append("', ")
//					.append("url='").append(url).append("' ")
//					.append(" where id = '").append(hostid).append("';");
//			
//			logger.info(sql);
//			
//			DataBaseOperation.execute(sql.toString());
			
		} catch (Exception e) {

			e.printStackTrace();
			
			logger.error("Failed to update the host table " + e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	
	

}
