package gw.tools;

import gw.database.DataBaseOperation;
import gw.ssh.SSHSession;
import gw.utils.BaseTool;
import gw.web.GeoweaverController;

public class HistoryTool {
	
	/**
	 * Stop the process and change the status to stopped
	 * @param history_id
	 */
	public static void stop(String history_id) {
		
		try {
			
			SSHSession session = GeoweaverController.sshSessionManager.sessionsByToken.get(history_id);
			
			if(session!=null) {
				
				session.getSsh().close(); //this line close the shell session and the associated command is stopped
				
			}
			
			String history_end_time = BaseTool.getCurrentMySQLDatetime();

			StringBuffer sql = new StringBuffer("update history set end_time = '");
			
			sql.append(history_end_time);
			
			sql.append("', indicator = 'Stopped' where id = '");
			
			sql.append(history_id).append("';");
			
			DataBaseOperation.execute(sql.toString());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
	}

}
