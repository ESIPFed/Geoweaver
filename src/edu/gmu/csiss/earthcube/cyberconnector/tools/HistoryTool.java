package edu.gmu.csiss.earthcube.cyberconnector.tools;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

public class HistoryTool {
	
	/**
	 * Stop the process and change the status to stopped
	 * @param history_id
	 */
	public static void stop(String history_id) {
		
		try {
			
			String history_end_time = BaseTool.getCurrentMySQLDatetime();

			StringBuffer sql = new StringBuffer("update history set end_time = '");
			
			sql.append(history_end_time);
			
			sql.append("', indicator = 'Stopped' where id = '");
			
			sql.append(history_id).append("';");
			
			DataBaseOperation.execute(sql.toString());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
	}

}
