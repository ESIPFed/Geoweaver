package gw.tools;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import gw.database.DataBaseOperation;
import gw.local.LocalSession;
import gw.local.LocalSessionNixImpl;
import gw.local.LocalSessionWinImpl;
import gw.ssh.SSHSession;
import gw.ssh.SSHSessionImpl;
import gw.tools.HostTool;
import gw.tools.ProcessTool;
import gw.utils.BaseTool;
import gw.utils.OSValidator;
import gw.utils.RandomString;
import gw.utils.SysDir;
import gw.web.GeoweaverController;

/**
 * 
 * Run things on localhost
 * 
 * @author jensensun
 *
 */
public class LocalhostTool {
	
	static Logger logger = Logger.getLogger(LocalhostTool.class);

	/**
	 * Execute Shell Script on Localhost
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
			
			LocalSession session = getLocalSession();
			
			session.runBash(code, id, isjoin, token); 
			
			String historyid = session.getHistory().getHistory_id();
			
			GeoweaverController.sshSessionManager.localSessionByToken.put(token, session);
			
			resp = "{\"history_id\": \""+historyid+
					
					"\", \"token\": \""+token+
					
					"\", \"ret\": \"success\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}  finally {
			
			GeoweaverController.sshSessionManager.closeWebSocketByToken(token); //close this websocket at the end
			
		}
        		
		return resp;
		
	}
	
	/**
	 * Get Local Session
	 * @return
	 * local session
	 */
	static LocalSession getLocalSession() {
		
		LocalSession session = null;
		
		if(OSValidator.isWindows()) {
			
			session = new LocalSessionWinImpl();
			
		}else if(OSValidator.isMac() || OSValidator.isUnix()) {
			
			session = new LocalSessionNixImpl();
			
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
	public static String executeBuiltInProcess(String id, String hid, String pswd, String token, boolean isjoin) {
		
		
		return null;
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
	public static String executeJupyterProcess(String id, String hid, String pswd, String token, boolean isjoin,
			String bin, String pyenv, String basedir) {
		// TODO Auto-generated method stub
		return null;
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
	public static String executePythonProcess(String id, String hid, String pswd, 
			String token, boolean isjoin, String bin, String pyenv, String basedir) {

		String resp = null;
		
		try {
			
			//write all the python files into local workspace folder
			localizeAllPython(token);
			
			//get code of the process
			
			String code = ProcessTool.getCodeById(id);
			
			LocalSession session = getLocalSession();
			
			GeoweaverController.sshSessionManager.localSessionByToken.put(token, session);
			
			session.runPython(code, id, isjoin, bin, pyenv, basedir, token); 
			
			String historyid = session.getHistory().getHistory_id();
			
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
	 * Remove all the leftover Python files
	 * @param hid
	 */
	public static void cleanAllPython(String hid) {
		
		String folderpath = SysDir.workspace + "/" + hid + "/";
		
		File folder = new File(folderpath);
		
		String[]entries = folder.list();
		
		for(String s: entries){
		
			File currentFile = new File(folder.getPath(),s);
		    
			currentFile.delete();
		
		}
		
		logger.info("The temp python files for " + hid + " have been deleted.");
		
	}
	
	/**
	 * Package all python files into one zip file
	 */
	public static void localizeAllPython(String hid) {
		
		StringBuffer sql = new StringBuffer("select name,code from process_type where description = 'python';");
		
		logger.info(sql.toString());
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		String code = null, name = null;
		
		try {
			
			String folderpath = SysDir.workspace + "/" + hid + "/";
			
			new File(folderpath).mkdirs(); //make a temporary folder
			
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
				
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
		
		
	}
	
}
