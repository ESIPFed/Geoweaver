package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.GeoweaverProcessTask;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.TaskManager;
import edu.gmu.csiss.earthcube.cyberconnector.tasks.TaskSocket;
import edu.gmu.csiss.earthcube.cyberconnector.tools.HistoryTool;
import edu.gmu.csiss.earthcube.cyberconnector.user.User;
import edu.gmu.csiss.earthcube.cyberconnector.user.UserTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.Message;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;
import edu.gmu.csiss.earthcube.cyberconnector.utils.SysDir;
import edu.gmu.csiss.earthcube.cyberconnector.web.GeoweaverController;
import net.schmizz.sshj.common.IOUtils;
import net.schmizz.sshj.connection.channel.direct.Session;

public class ProcessTool {
	
	static Logger logger = LoggerFactory.getLogger(ProcessTool.class);
	
	/**
	 * Get the list of processes
	 * @param owner
	 * @param isactive
	 * If this is true, only return the processes that are still running.
	 * @return
	 * @throws SQLException
	 */
	public static String list(String owner) throws SQLException {
		
		StringBuffer json = new StringBuffer("[");
		
		StringBuffer sql = new StringBuffer("select id, name, description from process_type");
		
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		int num = 0;
		
		while(rs.next()) {
			
			String id = rs.getString("id");
			
			String name = rs.getString("name");
			
			String desc = rs.getString("description");
			
			if(BaseTool.isNull(desc)) desc = "shell";
			
			if(!StringUtils.isNumeric(id)) {
				
				if(num!=0) {
					
					json.append(",");
					
				}
				
				json.append("{\"id\": \"").append(id).append("\", \"name\": \"")
					
					.append(name).append("\", \"desc\":\"").append(desc).append("\"}");

				num++;
				
			}
			
		}
		
		json.append("]");
		
		DataBaseOperation.closeConnection();
		
		return json.toString();
		
	}
	
	public static String detail(String id) {
		
		StringBuffer sql = new StringBuffer("select * from process_type where id = '").append(id).append("';");
		
		StringBuffer resp = new StringBuffer();
		
		try {

			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			if(rs.next()) {
				
				resp.append("{ \"id\":\"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"name\":\"").append(rs.getString("name")).append("\", ");
				
				String lang = "shell";
				
				if(!BaseTool.isNull(rs.getString("description")))
				
					lang = rs.getString("description");
				
				resp.append("\"description\":\"").append(lang).append("\", ");
				
				String code = rs.getString("code");

				if(lang.equals("jupyter")) {
					
//					String folderpath = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/";
//					
//					String filename = code;
//					
//					String filepath = folderpath + filename;
//					
//					code = BaseTool.readStringFromFile(filepath);
//					
////					code = escape(code);
//					
//					System.out.println(code);
					
					resp.append("\"code\":").append(code).append(" ");
					
				}else {
					
					code = escape(code);
					
					resp.append("\"code\":\"").append(code).append("\" ");
					
				}
				
				resp.append(" }");
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp.toString();
		
	}
	
	public static String escape_jupyter(String code) {		
		
		return null;
		
	}

	/**
	 * Escape the code text
	 * @param code
	 * @return
	 */
	public static String escape(String code) {
		
		String resp = null;
		
		if(!BaseTool.isNull(code))
		
			resp = code.replaceAll("\\\\", "\\\\\\\\")
					.replaceAll("\"", "\\\\\"")
					.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>")
					.replaceAll("	", "\\\\t");
		
//		logger.info(resp);
		
		return resp;
		
	}
	
	public static String unescape(String code) {
		
		String resp = code.replaceAll("-.-", "/").replaceAll("-·-", "'").replaceAll("-··-", "\"").replaceAll("->-", "\\n").replaceAll("-!-", "\\r");
		
		logger.info(resp);
		
		return resp;
		
	}
	
	public static void update(String id, String name, String lang, String code, String description) {
		
		StringBuffer sql = new StringBuffer("update process_type set name = '").append(name)
				
				.append("', code = ?, description = '").append(lang).append("' where id = '").append(id).append("';");
		
		logger.info(sql.toString());
		
		DataBaseOperation.preexecute(sql.toString(), new String[] {code});
		
	}
	/**
	 * add the process to the local file
	 * @param name
	 * @param lang
	 * @param code
	 * @param desc
	 * @return
	 */
	public static String add_local(String name, String lang, String code, String desc) {
		
		String folderpath = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/";
		
		String filename = "jupyter-code-" + new RandomString(7).nextString();
		
		String filepath = folderpath + filename;
		
		BaseTool.writeString2File(code, filepath);
		
		String newid = new RandomString(6).nextString();
		
		StringBuffer sql = new StringBuffer("insert into process_type (id, name, code, description) values ('");
		
		sql.append(newid).append("', '");
		
		sql.append(name).append("', ?, '");
		
		sql.append(desc).append("'); ");
		
		logger.info(sql.toString());
		
		DataBaseOperation.preexecute(sql.toString(), new String[] {filename});
		
		return newid;
		
	}
	
	/**
	 * add code to database
	 * @param name
	 * @param lang
	 * @param code
	 * @param description
	 * @return
	 */
	public static String add_database(String name, String lang, String code, String desc) {

		String newid = new RandomString(6).nextString();
		
		StringBuffer sql = new StringBuffer("insert into process_type (id, name, code, description) values ('");
		
		sql.append(newid).append("', '");
		
		sql.append(name).append("', ?, '");
		
		sql.append(desc).append("'); ");
		
		logger.info(sql.toString());
		
		DataBaseOperation.preexecute(sql.toString(), new String[] {code});
		
		return newid;
		
	}
	
	/**
	 * Add local file into database with fixed file location and server id
	 * @param name
	 * @param type
	 * @param code
	 * @param filepath
	 * @param hid
	 * @return
	 */
	public static String add_database(String name, String type, String code, String filepath, String hid) {
		
		String newid = null;
		
		try {
			
			//check if the file is already in the database. If yes, should replace the process content only instead of inserting a new row.
			StringBuffer sql = new StringBuffer("select * from process_type where inputs = '")
					.append(filepath).append("' and inputs_datatypes = '").append(hid).append("'; ");
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			if(rs.next()) {
				
				sql = new StringBuffer("update process_type set code = ? where inputs = '")
					.append(filepath).append("' and inputs_datatypes = '").append(hid).append("'; ");
				
				logger.info(sql.toString());
				
				DataBaseOperation.preexecute(sql.toString(), new String[] {code});
				
			}else {
				
				newid = new RandomString(6).nextString();
				
				sql = new StringBuffer("insert into process_type (id, name, code, description, inputs, inputs_datatypes) values ('");
				
				sql.append(newid).append("', '");
				
				sql.append(name).append("', ?, '");
				
				sql.append(type).append("', '");
				
				sql.append(filepath).append("', '");
				
				sql.append(hid).append("'); ");
				
				logger.info(sql.toString());
				
				DataBaseOperation.preexecute(sql.toString(), new String[] {code});
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
		}
		
		return newid;
		
	}
	
	/**
	 * Add process
	 * @param name
	 * @param lang
	 * @param code
	 * @param desc
	 * @return
	 */
	public static String add(String name, String lang, String code, String desc) {
		
		String newid = null;
		
//		if(lang.equals("jupyter")) {
//			
//			newid = ProcessTool.add_database(name, lang, code, desc); //jupyter still goes to the database
//			
//		}else {
			
			newid = ProcessTool.add_database(name, lang, code, desc);
			
//		}
		
		return newid;
		
	}
	
	
	public static String del(String id) {
		
		StringBuffer sql = new StringBuffer("delete from process_type where id = '").append(id).append("';");
		
		DataBaseOperation.execute(sql.toString());
		
		return "done";
		
	}
	
	/**
	 * Get process name by id
	 * @param pid
	 * @return
	 */
	public static String getNameById(String pid) {

		StringBuffer sql = new StringBuffer("select name from process_type where id = '").append(pid).append("';");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		String name = null;
		
		try {
			
			if(rs.next()) {
				
				name = rs.getString("name");
				
			}
			
			DataBaseOperation.closeConnection();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return name;
		
	}
	
	/**
	 * Get code by Id
	 * @param pid
	 * @return
	 */
	public static String getCodeById(String pid) {
		
		StringBuffer sql = new StringBuffer("select name,code from process_type where id = '").append(pid).append("';");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		String code = null;
		
		try {
			
			if(rs.next()) {
				
				code = rs.getString("code");
				
			}
			
			DataBaseOperation.closeConnection();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return code;
		
	}
	
	/**
	 * get category of the process,e.g. shell, python, R, java, geoweaver-builtin, etc. 
	 * @param pid
	 * @return
	 */
	public static String getTypeById(String pid) {
		
		StringBuffer sql = new StringBuffer("select description from process_type where id = '").append(pid).append("';");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		String desc = null;
		
		try {
			
			if(rs.next()) {
				
				desc = rs.getString("description");
				
			}
			
			DataBaseOperation.closeConnection();
			
			if(BaseTool.isNull(desc)) desc = "shell"; //default shell
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return desc;
		
		
	}
	
//	public static String executeLocal(String hid, String filepath, String type) {
//		
//		String resp = null;
//		
//		if("python".equals(type)) {
//			
//			
//			
//		}else if("shell".equals(type)) {
//			
//			executeShell(hid, filepath);
//			
//		}
//		
//		return resp;
//		
//	}
//	
//	/**
//	 * Execute local shell script
//	 * @param hid
//	 * @param filepath
//	 * @return
//	 */
//	public static String executeShell(String hid, String filepath, String pswd, String token) {
//		
//		String resp = null;
//		
//		try {
//			
//			if(token == null) {
//				
//				token = new RandomString(12).nextString();
//				
//			}
//			
//			SSHSession session = new SSHSessionImpl();
//			
//			session.login(hid, pswd, token, false);
//			
//			GeoweaverController.sshSessionManager.sessionsByToken.put(token, session);
//			
////			session.runBash(code, id, isjoin); 
//			
//			
//			String historyid = session.getHistory_id();
//			
//			resp = "{\"history_id\": \""+historyid+
//					
//					"\", \"token\": \""+token+
//					
//					"\", \"ret\": \"success\"}";
//			
//			
//		}catch(Exception e) {
//			
//			e.printStackTrace();
//			
//		}
//		
//		return resp;
//		
//	}
	
	/**
	 * Execute shell scripts
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @return
	 */
	public static String executeShell(String id, String hid, String pswd, String token, boolean isjoin) {
		

		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = getCodeById(id);
			
			System.out.println(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sshSessionManager.sessionsByToken.put(token, session);
			
			session.runBash(code, id, isjoin); 
			
			String historyid = session.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
//			SSHCmdSessionOutput task = new SSHCmdSessionOutput(code);
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sshSessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Execute jupyter process
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @return
	 */
	public static String executeJupyterProcess(String id, String hid, String pswd, String token, 
			boolean isjoin, String bin, String pyenv, String basedir) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = getCodeById(id);
			
			System.out.println(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sshSessionManager.sessionsByToken.put(token, session);
			
			session.runJupyter(code, id, isjoin, bin, pyenv, basedir); 
			
			String historyid = session.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			//save environment
			
			HostTool.addEnv(historyid, hid, "python", bin, pyenv, basedir, "");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sshSessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
		
		return resp;
		
	}
	
	/**
	 * Execute builtin process
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @return
	 */
	public static String executeBuiltInProcess(String id, String hid, String pswd, String token, boolean isjoin) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = getCodeById(id);
			
			logger.debug(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
//			SSHSession session = new SSHSessionImpl();
//			
//			session.login(hid, pswd, token, false);
//			
//			GeoweaverController.sshSessionManager.sessionsByToken.put(token, session);
//			
//			session.runBash(code, id, isjoin); 
			
//			String historyid = session.getHistory_id();
			
			GeoweaverProcessTask t = new GeoweaverProcessTask(token);
			
			t.initialize(id, hid, pswd, token, isjoin);
			
			// find active websocket for this builtin process when it is running as a member process in a workflow
			// If this builtin process is running solo, the TaskSocket will take care of the problem.
			
			WebSocketSession ws = TaskSocket.findSessionById(WorkflowTool.token2ws.get(token));
			
			if(!BaseTool.isNull(ws)) t.startMonitor(ws);
			
			if(isjoin) {
			
				TaskManager.runDirectly(t);
				
			}else {
			
				TaskManager.addANewTask(t);
				
			}
			
			String historyid = t.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
//			SSHCmdSessionOutput task = new SSHCmdSessionOutput(code);
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sshSessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Package all python files into one zip file
	 */
	public static String packageAllPython(String hid) {
		
		StringBuffer sql = new StringBuffer("select name,code from process_type where description = 'python';");
		
		logger.info(sql.toString());
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		String resp = null, code = null, name = null;
		
		try {
			
			String folderpath = BaseTool.getCyberConnectorRootPath() + SysDir.temp_file_path + "/" + hid + "/";
			
			resp = BaseTool.getCyberConnectorRootPath() + SysDir.temp_file_path + "/" + hid + ".tar";
			
			new File(folderpath).mkdirs(); //make a temporary folder
			
			List<String> files = new ArrayList();
			
			while(rs.next()) {
				
				code = rs.getString("code");
				
				name = rs.getString("name");
				
				String filepath = folderpath;
				
				if(name.endsWith(".py")) {
					
					filepath += name;
					
				}else{
				
					filepath += name + ".py";
					
				}
				
				logger.info(filepath);
				
				BaseTool.writeString2File(code, filepath);
				
				files.add(filepath);
				
			}
			
			if(files.size()==0) {
				
				throw new RuntimeException("No python is found in the database");
				
			}
			//zip the files into a tar file
			BaseTool.tar(files, resp);
			
			DataBaseOperation.closeConnection();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}
		
		return resp;
		
	}
	
	/**
	 * Execute Python process
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * for security reasons
	 * @param isjoin
	 * @return
	 */
	public static String executePythonProcess(String id, String hid, String pswd, 
			String token, boolean isjoin, String bin, String pyenv, String basedir) {

		String resp = null;
		
		try {
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			//package all the python files into a tar
			String packagefile = ProcessTool.packageAllPython(token);
			
			if(basedir!=null) {
				
				FileTool.scp_upload(hid, pswd, packagefile, basedir, true);
				
			}else {
				
				FileTool.scp_upload(hid, pswd, packagefile);
				
			}
			
			//get code of the process
			
			String code = getCodeById(id);
			
//			logger.info(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sshSessionManager.sessionsByToken.put(token, session);
			
			session.runPython(code, id, isjoin, bin, pyenv, basedir, token); 
			
			String historyid = session.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			//save environment
			
			HostTool.addEnv(historyid, hid, "python", bin, pyenv, basedir, "");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sshSessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
		
		return resp;
		
	}
	
	/**
	 * For Andrew
	 * @param hisid
	 * @return
	 */
	public static String stop(String hisid) {
		
		String resp = null;
		
		try {
			
			SSHSession session = GeoweaverController.sshSessionManager.sessionsByToken.get(hisid);
			
			if(!BaseTool.isNull(session))
				
				session.getSSHJSession().close();
			
			//establish SSH session and generate a token for it
//			
//			if(token == null) {
//				
//				token = new RandomString(12).nextString();
//				
//			}
//			
//			SSHSession session = new SSHSessionImpl();
//			
//			session.login(hid, pswd, token, false);
//			
//			GeoweaverController.sshSessionManager.sessionsByToken.put(token, session);
//			
//			String code = "#!/bin/bash\n" + 
//					"kill -9 " + hid;
//			
//			session.runBash(code, id, isjoin); 
//			
			HistoryTool.stop(hisid);
//				
			resp = "{\"history_id\": \""+hisid+
//					
//					"\", \"token\": \""+token+
//					
					"\", \"ret\": \"stopped\"}";
			
//			SSHCmdSessionOutput task = new SSHCmdSessionOutput(code);
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Execute one process on a host
	 * @param id
	 * process Id
	 * @param hid
	 * host Id
	 * @param pswd
	 * password
	 * @return
	 */
	public static String execute(String id, String hid, String pswd, String token, 
			boolean isjoin, String bin, String pyenv, String basedir) {
		
		String category = getTypeById(id);
		
		logger.info("this process is : " + category);
		
		String resp = null;
		
		if("shell".equals(category)) {
			
			resp = executeShell(id, hid, pswd, token, isjoin);
			
		}else if("builtin".equals(category)) {
			
			resp = executeBuiltInProcess(id, hid, pswd, token, isjoin);
			
		}else if("jupyter".equals(category)){
			
			resp = executeJupyterProcess(id, hid, pswd, token, isjoin, bin, pyenv, basedir);
			
		}else if("python".equals(category)) {
			
			resp = executePythonProcess(id, hid, pswd, token, isjoin, bin, pyenv, basedir);
			
		}else{
			
			throw new RuntimeException("This category of process is not supported");
			
		}

		return resp;
		
	}

	public static String recent(int limit) {
		
		StringBuffer resp = new StringBuffer();
		
		StringBuffer sql = new StringBuffer("select * from history, process_type where process_type.id = history.process ORDER BY begin_time DESC limit ").append(limit).append(";");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"name\": \"").append(rs.getString("name")).append("\", ");
				
				resp.append("\"end_time\": \"").append(rs.getString("end_time")).append("\", ");
				
				resp.append("\"status\": \"").append(rs.getString("indicator")).append("\", ");
				
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
	 * get all details of one history
	 * @param hid
	 * @return
	 */
	public static String one_history(String hid) {
		
		StringBuffer resp = new StringBuffer();
		
		StringBuffer sql = new StringBuffer("select * from history, process_type where history.id = '").append(hid).append("' and history.process=process_type.id;");
		
		try {
			
			ResultSet rs = DataBaseOperation.query(sql.toString());
			
			if(rs.next()) {
				
				resp.append("{ \"id\": \"").append(rs.getString("history.id")).append("\", ");
				
				resp.append("\"process\": \"").append(rs.getString("process")).append("\", ");
				
				resp.append("\"name\": \"").append(rs.getString("name")).append("\", ");
				
				resp.append("\"begin_time\":\"").append(rs.getString("begin_time")).append("\", ");
				
				resp.append("\"end_time\":\"").append(rs.getString("end_time")).append("\", ");
				
				resp.append("\"input\":\"").append(escape(rs.getString("input"))).append("\", ");
				
				resp.append("\"output\":\"").append(escape(rs.getString("output"))).append("\", ");
				
				resp.append("\"host\":\"").append(escape(rs.getString("host"))).append("\", ");
				
				resp.append("\"status\":\"").append(rs.getString("indicator")).append("\" }");
				
			}
			
		} catch (SQLException e) {
		
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	/**
	 * Get all active processes
	 * @return
	 */
	public static String all_active_process() {
		
		StringBuffer resp = new StringBuffer();
		
		StringBuffer sql = new StringBuffer("select * from history where indicator='Running'  ORDER BY begin_time DESC;");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"begin_time\": \"").append(rs.getString("begin_time"));
				
				resp.append("\", \"end_time\": \"").append(rs.getString("end_time"));
				
				resp.append("\", \"output\": \"").append(escape(rs.getString("output")));
				
				resp.append("\", \"status\": \"").append(escape(rs.getString("indicator")));
				
				resp.append("\", \"host\": \"").append(escape(rs.getString("host")));
				
				resp.append("\"}");
				
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
	 * get all the execution history of this process
	 * @param pid
	 * @return
	 */
	public static String all_history(String pid) {
		
		StringBuffer resp = new StringBuffer() ;
		
		StringBuffer sql = new StringBuffer("select * from history where process = '").append(pid).append("'  ORDER BY begin_time DESC;");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"begin_time\": \"").append(rs.getString("begin_time"));
				
				resp.append("\", \"end_time\": \"").append(rs.getString("end_time"));
				
				resp.append("\", \"output\": \"").append(escape(rs.getString("output")));
				
				resp.append("\", \"status\": \"").append(escape(rs.getString("indicator")));
				
				resp.append("\", \"host\": \"").append(escape(rs.getString("host")));
				
				resp.append("\"}");
				
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
	
	
	
	
}
