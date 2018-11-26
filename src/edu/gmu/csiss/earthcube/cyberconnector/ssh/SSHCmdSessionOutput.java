package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSHCmdSessionOutput  implements Runnable {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	public SSHCmdSessionOutput(String code) {
		
		//feed the process code into the SSH session
		
//		String executebash = "echo \"" + code.replaceAll("\"", "\\\\\"") + "\" > geoweaver-" + token + ".sh;"+
//		
//				"chmod +x geoweaver-" + token + ".sh; " + 
//				
//				"./geoweaver-" + token + ".sh;";
//		
//		Session.Command cmd = session.getSSHJSession().exec(executebash);
//		
//		String output = IOUtils.readFully(cmd.getInputStream()).toString();
//		
//		logger.info(output);
//		
//		//wait until the process execution is over
//		
//        cmd.join(5, TimeUnit.SECONDS);
//        
//		cmd.close();
//		
//		session.logout();
//		
//		GeoweaverController.sshSessionManager.sessionsByToken.remove(token);
		
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
		
	}
	
	
}
