package com.gw.tools;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.gw.database.DataBaseOperation;
import com.gw.database.ProcessRepository;
import com.gw.jpa.GWProcess;
import com.gw.ssh.SSHSession;
import com.gw.ssh.SSHSessionImpl;
import com.gw.tasks.GeoweaverProcessTask;
import com.gw.tasks.TaskManager;
import com.gw.tasks.TaskSocket;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.utils.SysDir;
import com.gw.web.GeoweaverController;
import  com.gw.server.CommandServlet;

@Service
public class RemotehostTool {
	
	Logger logger = LoggerFactory.getLogger(ProcessTool.class);
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	TaskManager tm;
	
	@Value("${geoweaver.temp_file_path}")
	String temp_file_path;
	
	@Autowired
	FileTool ft;
	
	@Autowired
	HostTool ht;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	SSHSession session;
	
	@Autowired
	ProcessRepository processrepository;

	@Autowired
	GeoweaverProcessTask t ;
	
	/**
	 * Execute shell scripts
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @return
	 */
	public String executeShell(String id, String hid, String pswd, String token, boolean isjoin) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
			logger.debug(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
//			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			session.runBash(code, id, isjoin, token); 
			
			String historyid = session.getHistory_id();
			
			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
			
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
//			GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
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
	public String executeJupyterProcess(String id, String hid, String pswd, String token, 
			boolean isjoin, String bin, String pyenv, String basedir) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
			logger.debug(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
//			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
			
			session.runJupyter(code, id, isjoin, bin, pyenv, basedir, token); 
			
			String historyid = session.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			//save environment
			
			ht.addEnv(historyid, hid, "python", bin, pyenv, basedir, "");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
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
	public String executeBuiltInProcess(String id, String hid, String pswd, String token, boolean isjoin) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
			logger.debug(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			// GeoweaverProcessTask t = new GeoweaverProcessTask();
			
			t.initialize(id, hid, pswd, token, isjoin,  null, null, null);
			
			// find active websocket for this builtin process when it is running as a member process in a workflow
			// If this builtin process is running solo, the TaskSocket will take care of the problem.
			
			if(isjoin) {
			
				tm.runDirectly(t);
				
			}else {
			
				tm.addANewTask(t);
				
			}
			
			String historyid = t.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Package all python files into one zip file
	 */
	public String packageAllPython(String hid) {
		
//		StringBuffer sql = new StringBuffer("select name,code from process_type where description = 'python';");
//		
//		logger.info(sql.toString());
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		Collection<GWProcess> process_list = processrepository.findPythonProcess();
		
		String resp = null, code = null, name = null;
		
		try {
			
			String folderpath = bt.getFileTransferFolder() + "/" + hid + "/";
			
			resp = bt.getFileTransferFolder() + "/" + hid + ".tar";
			
			new File(folderpath).mkdirs(); //make a temporary folder
			
			List<String> files = new ArrayList();
			
			Iterator<GWProcess> it = process_list.iterator();
			
			while(it.hasNext()) {
				
				GWProcess p = it.next();
				
				code = p.getCode();
				
				code = pt.unescape(code);
				
				name = p.getName();
				
				String filepath = folderpath;
				
				if(name.endsWith(".py")) {
					
					filepath += name;
					
				}else{
				
					filepath += name + ".py";
					
				}
				
				logger.debug(filepath);
				
				bt.writeString2File(code, filepath);
				
				files.add(filepath);
				
			}
			
			if(files.size()==0) {
				
				throw new RuntimeException("No python is found in the database");
				
			}
			
			//zip the files into a tar file
			bt.tar(files, resp);
			
		} catch (Exception e) {
			
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
	public String executePythonProcess(String id, String hid, String pswd, 
			String token, boolean isjoin, String bin, String pyenv, String basedir) {

		String resp = null;
		
		try {
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			//package all the python files into a tar
			String packagefile = packageAllPython(token);
			
			if(basedir!=null) {
				
				ft.scp_upload(hid, pswd, packagefile, basedir, true);
				
			}else {
				
				ft.scp_upload(hid, pswd, packagefile);
				
			}
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
//			logger.info(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			//establish SSH session and generate a token for it
			
//			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
			
			session.runPython(code, id, isjoin, bin, pyenv, basedir, token); 
			
			String historyid = session.getHistory_id();
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			//save environment
			
			ht.addEnv(historyid, hid, "python", bin, pyenv, basedir, "");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
		
		return resp;
		
	}

}
