package com.gw.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import com.gw.database.ProcessRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.ssh.SSHSession;
import com.gw.tasks.GeoweaverProcessTask;
import com.gw.tasks.TaskManager;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.web.GeoweaverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class RemotehostTool {
	
	Logger logger = LoggerFactory.getLogger(ProcessTool.class);
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	TaskManager tm;
	
	@Value("${geoweaver.temp_file_path}")
	String temp_file_path;

	@Value("${geoweaver.workspace}")
    private String           workspace_folder_path;
	
	@Autowired
	FileTool ft;
	
	@Autowired
	HostTool ht;
	
	@Autowired
	BaseTool bt;

	@Autowired
	EnvironmentTool et;

	@Autowired
	HistoryTool histool;
	
	@Autowired
	SSHSession session;
	
	@Autowired
	ProcessRepository processrepository;

	// @Autowired
	// GeoweaverProcessTask t ;

	/**
	 * Find all python environments
	 * @param hostid
	 * @param password
	 * @return
	 */
	public String readPythonEnvironment(String hostid, String password){

		session.login(hostid, password, new RandomString(18).nextString(), false);

		return session.readPythonEnvironment(hostid, password);

	}
	
	/**
	 * Execute shell scripts
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @return
	 */
	public String executeShell(String history_id, String id, String hid, String pswd, String token, boolean isjoin) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
			logger.debug(code);

			this.saveHistory(id, code, history_id);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}

			//package all the python files into a tar
			String packagefile = packageAllPython(history_id);
			
			ft.scp_upload(hid, pswd, packagefile); //upload the python files to the home directory
			
//			SSHSession session = new SSHSessionImpl();
			
			session.login(hid, pswd, token, false);
			
			session.runBash(history_id, code, id, isjoin, token); 
			
			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
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
	public String executeJupyterProcess(String history_id, String id, String hid, String pswd, String token, 
			boolean isjoin, String bin, String pyenv, String basedir) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
			logger.debug(code);

			this.saveHistory(id, code, history_id);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
			
			session.runJupyter(history_id, code, id, isjoin, bin, pyenv, basedir, token); 
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			//save environment
			
			et.addEnv(history_id, hid, "python", bin, pyenv, basedir, "");
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			// GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
		
		return resp;
		
	}
	
	/**
	 * Execute builtin process - not implemented yet
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @return
	 */
	public String executeBuiltInProcess(String history_id, String id, String hid, String pswd, String token, boolean isjoin) {
		
		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = pt.getCodeById(id);
			
			logger.debug(code);

			this.saveHistory(id, code, history_id);
			
			//establish SSH session and generate a token for it
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			// GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
        		
		return resp;
		
	}

	/**
	 * Package all python files into one zip file
	 */
	public String packageAllPython(String hid) {
		
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


	public void saveHistory(String processid, String script, String history_id){

		History history = histool.getHistoryById(history_id);

		if(BaseTool.isNull(history)){

			history = new History();

			history.setHistory_id(history_id);

		}

		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(BaseTool.getCurrentSQLDate());
		
		history.setHistory_input(script);

        history.setHistory_id(history_id);

		histool.saveHistory(history);

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
	public String executePythonProcess(String history_id, String id, String hid, String pswd, 
			String token, boolean isjoin, String bin, String pyenv, String basedir) {

		String resp = null;
		
		try {
			
			if(token == null) {
				
				token = new RandomString(12).nextString();
				
			}
			
			//package all the python files into a tar
			String packagefile = packageAllPython(history_id);
			
			if(basedir!=null && !"~".equals(basedir)) {
				
				ft.scp_upload(hid, pswd, packagefile, basedir, true);
				
			}else {
				
				ft.scp_upload(hid, pswd, packagefile);
				
			}
			
			//get code of the process
			
			String code = pt.getCodeById(id);

			this.saveHistory(id, code, history_id);
			
			session.login(hid, pswd, token, false);
			
			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
			//save environment
			
			et.addEnv(history_id, hid, "python", bin, pyenv, basedir, "");
			
			session.runPython(history_id, code, id, isjoin, bin, pyenv, basedir, token); 
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			// GeoweaverController.sessionManager.closeWebSocketByToken(token); //close this websocket at the end - don't close - the websocket channel should stay on
			
		}
		
		return resp;
		
	}

}
