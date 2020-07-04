package gw.tools;

import org.json.simple.JSONObject;

import gw.local.LocalSession;
import gw.local.LocalSessionNixImpl;
import gw.local.LocalSessionWinImpl;
import gw.ssh.SSHSession;
import gw.ssh.SSHSessionImpl;
import gw.tools.HostTool;
import gw.tools.ProcessTool;
import gw.utils.OSValidator;
import gw.utils.RandomString;
import gw.web.GeoweaverController;

/**
 * 
 * Run things on localhost
 * 
 * @author jensensun
 *
 */
public class LocalhostTool {

	public static String executeShell(String id, String hid, String pswd, String token, boolean isjoin) {
		

		String resp = null;
		
		try {
			
			//get code of the process
			
			String code = ProcessTool.getCodeById(id);
			
			System.out.println(code);
			
			//get host ip, port, user name and password
			
//			String[] hostdetails = HostTool.getHostDetailsById(hid);
			
			LocalSession session = null;
			
			if(OSValidator.isWindows()) {
				
				session = new LocalSessionWinImpl();
				
			}else if(OSValidator.isMac() || OSValidator.isUnix()) {
				
				session = new LocalSessionNixImpl();
				
			}else {
				
				throw new RuntimeException("This operating system is not supported as localhost.");
				
			}
			
			session.runBash(code, id, isjoin, token); 
			
			String historyid = session.getHistory().getHistory_id();
			
			GeoweaverController.sshSessionManager.localSessionByToken.put(token, session);
			
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

	public static String executeBuiltInProcess(String id, String hid, String pswd, String token, boolean isjoin) {
		
		
		return null;
	}

	public static String executeJupyterProcess(String id, String hid, String pswd, String token, boolean isjoin,
			String bin, String pyenv, String basedir) {
		// TODO Auto-generated method stub
		return null;
	}

	public static String executePythonProcess(String id, String hid, String pswd, String token, boolean isjoin,
			String bin, String pyenv, String basedir) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
