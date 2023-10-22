package com.gw.tools;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import com.gw.database.ProcessRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.local.LocalSession;
import com.gw.local.LocalSessionNixImpl;
import com.gw.local.LocalSessionWinImpl;
import com.gw.tasks.GeoweaverProcessTask;
import com.gw.tasks.TaskManager;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.OSValidator;
import com.gw.web.GeoweaverController;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 
 * Run things on localhost
 * 
 * @author jensensun
 *
 */
@Service
@Scope("prototype")
public class LocalhostTool {
	
	Logger logger = Logger.getLogger(LocalhostTool.class);
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	TaskManager tm;
	
	@Autowired
	HostTool ht;

	@Autowired
	BuiltinTool bint;

	@Autowired
	EnvironmentTool et;
	
	@Value("${geoweaver.workspace}")
	String workspace_folder_path;
	
	@Autowired
	BaseTool bt;

	@Autowired
	HistoryTool histool;
	
	@Autowired
	ProcessRepository processrepository;
	
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

	public String readPythonEnvironment(String hostid, String password){

		LocalSession session = this.getLocalSession();

		return session.readPythonEnvironment(hostid, password);

	}

	public void authenticate(String password) throws Exception{

		if(!bt.checkLocalhostPassword(password)){

			throw new RuntimeException("Authentication Failed. Wrong Password.");

		}

	}

	/**
	 * Execute Shell Script on Localhost
	 * @param history_id
	 * @param id
	 * process id
	 * @param hid
	 * host id
	 * @param pswd
	 * password
	 * @param token
	 * http session id
	 * @param isjoin
	 * if wait for the process to end
	 * @return
	 */
	public String executeShell(String history_id, String id, String hid, String pswd, String token, boolean isjoin) {
		
		String resp = null;
		
		try {

			authenticate(pswd);

			//write all the python files into local workspace folder
			localizeAllPython(history_id);
			
			//get code of the process
			String code = pt.getCodeById(id);
			
			this.saveHistory(id, code, history_id);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			LocalSession session = getLocalSession();
			
			session.runBash(history_id, code, id, isjoin, token); 
			
			GeoweaverController.sessionManager.localSessionByToken.put(token, session);

			GeoweaverController.sessionManager.localSessionByToken.put(history_id, session);
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Get Local Session
	 * @return
	 * local session
	 */
	public LocalSession getLocalSession() {
		
		LocalSession session = null;
		
		if(OSValidator.isWindows()) {
			
			session = BeanTool.getBean(LocalSessionWinImpl.class);
			
		}else if(OSValidator.isMac() || OSValidator.isUnix()) {
			
			session = BeanTool.getBean(LocalSessionNixImpl.class);
			
		}else {
			
			throw new RuntimeException("This operating system is not supported as localhost.");
			
		}
		
		return session;
		
	}

	/**
	 * Execute Built-in Process on Localhost
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

			authenticate(pswd);
			
			resp = bint.executeCommonTasks(history_id, id, hid, pswd, token, isjoin);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			
//			SSHSession session = new SSHSessionImpl();
//			
//			session.login(hid, pswd, token, false);
//			
//			GeoweaverController.sessionManager.sshSessionByToken.put(token, session);
//			
//			session.runBash(code, id, isjoin); 
			
//			String historyid = session.getHistory_id();
			
			// GeoweaverProcessTask t = new GeoweaverProcessTask();
			
			// t.initialize(id, hid, pswd, token, isjoin, token);
			
			// // find active websocket for this builtin process when it is running as a member process in a workflow
			// // If this builtin process is running solo, the TaskSocket will take care of the problem.
			
			// javax.websocket.Session ws = CommandServlet.findSessionById(token);
			
			// if(!BaseTool.isNull(ws)) t.startMonitor(ws);
			
			// if(isjoin) {
			
			// 	tm.runDirectly(t);
				
			// }else {
			
			// 	tm.addANewTask(t);
				
			// }

			// this.history_input = code;
			
			
			
			
//			SSHCmdSessionOutput task = new SSHCmdSessionOutput(code);
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
        		
		return resp;
		
	}

	/**
	 * Execute Jupyter Process on Localhost
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @return
	 */
	public String executeJupyterProcess(String history_id, String id, String hid, 
			String pswd, String token, boolean isjoin,
			String bin, String pyenv, String basedir) {

		String resp = null;
		
		try {
			
			authenticate(pswd);

			//write all the python files into local workspace folder
			localizeAllPython(history_id);

			//get code of the process
			
//			String code = ProcessTool.getCodeById(id);
			
			GWProcess process = pt.getProcessById(id);

			this.saveHistory(id, process.getCode(), history_id);

			String code = pt.getCodeById(id);
			
			localizeJupyter(code, process.getName(), history_id);
			
			LocalSession session = getLocalSession();
			
			GeoweaverController.sessionManager.localSessionByToken.put(token, session);

			GeoweaverController.sessionManager.localSessionByToken.put(history_id, session);
			
			//save environment
			
			et.addEnv(history_id, hid, "python", bin, pyenv, basedir, "");
			
			session.runJupyter(history_id, code, id, isjoin, bin, pyenv, basedir, token); 
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	
	
	/**
	 * Execute Python Process on Localhost
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param token
	 * http session id, cannot be null
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @return
	 */
	public String executePythonProcess(String history_id, String id, String hid, String pswd, 
			String token, boolean isjoin, String bin, String pyenv, String basedir) {

		String resp = null;
		
		try {

			authenticate(pswd);
			
			//write all the python files into local workspace folder
			localizeAllPython(history_id);
			
			//get code of the process
			
			String code = pt.getCodeById(id);

			this.saveHistory(id, code, history_id);
			
			LocalSession session = getLocalSession();
			
			GeoweaverController.sessionManager.localSessionByToken.put(token, session);

			GeoweaverController.sessionManager.localSessionByToken.put(history_id, session);

			//save environment
			et.addEnv(history_id, hid, "python", bin, pyenv, basedir, "");
			
			session.runPython(history_id, code, id, isjoin, bin, pyenv, basedir, token); 
			
			
			resp = "{\"history_id\": \""+history_id+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
			
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
		return resp;
		
	}
	
	/**
	 * Remove all the leftover Python files
	 * @param hid
	 */
	public void cleanAllPython(String hid) {
		
		String folderpath = bt.normalizedPath(workspace_folder_path) + "/" + hid + "/";
		
		File folder = new File(folderpath);
		
		String[]entries = folder.list();
		
		for(String s: entries){
		
			File currentFile = new File(folder.getPath(),s);
		    
			currentFile.delete();
		
		}
		
		logger.debug("The temp python files for " + hid + " have been deleted.");
		
	}
	
	/**
	 * Save Jupyter into the Local Workspace Folder if it is run on Localhost
	 * @param code
	 * @param token
	 */
	public void localizeJupyter(String code, String name, String history_id) {
		
		File workfolder = new File(bt.normalizedPath(workspace_folder_path) + "/" + history_id);
		
		if(!workfolder.exists()) {
			
			workfolder.mkdirs();
			
		}
		
		if(!name.endsWith(".ipynb")) {
			
			name += ".ipynb";
			
		}
		
		bt.writeString2File(code, workfolder + "/" + name);
		
	}
	
	/**
	 * Package all python files into one zip file
	 */
	public void localizeAllPython(String hid) {
		
		Collection<GWProcess> pythonprocesses = processrepository.findPythonProcess();
		
		String code = null, name = null;
		
		try {
			
			String folderpath = bt.normalizedPath(workspace_folder_path) + "/" + hid + "/";
			
			new File(folderpath).mkdirs(); //make a temporary folder
			
			Iterator<GWProcess> git = pythonprocesses.iterator();
			
			while(git.hasNext()) {
				
				GWProcess p = git.next();
				
				code = p.getCode();
				
				// code = pt.unescape(code);
				
				name = p.getName();
				
				String filepath = folderpath;
				
				if(name.endsWith(".py")) {
					
					filepath += name;
					
				}else{
				
					filepath += name + ".py";
					
				}
				
				logger.debug(filepath);
				
				bt.writeString2File(code, filepath);
				
			}
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		
	}
	
}
