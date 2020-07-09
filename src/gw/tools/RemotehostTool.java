package gw.tools;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import gw.database.DataBaseOperation;
import gw.ssh.SSHSession;
import gw.ssh.SSHSessionImpl;
import gw.tasks.GeoweaverProcessTask;
import gw.tasks.TaskManager;
import gw.tasks.TaskSocket;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;
import gw.web.GeoweaverController;

public class RemotehostTool {
	
	static Logger logger = LoggerFactory.getLogger(ProcessTool.class);
	
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
			
			String code = ProcessTool.getCodeById(id);
			
			System.out.println(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			session.runBash(code, id, isjoin, token); 
			
			String historyid = session.getHistory_id();
			
			GeoweaverController.sshSessionManager.sshSessionByToken.put(token, session);
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
//			SSHCmdSessionOutput task = new SSHCmdSessionOutput(code);
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			//the websocket persists
//			GeoweaverController.sshSessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
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
			
			String code = ProcessTool.getCodeById(id);
			
			System.out.println(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sshSessionManager.sshSessionByToken.put(token, session);
			
			session.runJupyter(code, id, isjoin, bin, pyenv, basedir, token); 
			
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
			
			String code = ProcessTool.getCodeById(id);
			
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
//			GeoweaverController.sshSessionManager.sshSessionByToken.put(token, session);
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
			String packagefile = packageAllPython(token);
			
			if(basedir!=null) {
				
				FileTool.scp_upload(hid, pswd, packagefile, basedir, true);
				
			}else {
				
				FileTool.scp_upload(hid, pswd, packagefile);
				
			}
			
			//get code of the process
			
			String code = ProcessTool.getCodeById(id);
			
//			logger.info(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sshSessionManager.sshSessionByToken.put(token, session);
			
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

}
