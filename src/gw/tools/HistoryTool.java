package gw.tools;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import gw.database.DataBaseOperation;
import gw.log.History;
import gw.ssh.SSHSession;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;
import gw.web.GeoweaverController;
/**
 * 
 * All the actions related to the History table
 * 
 * @author JensenSun
 *
 */
public class HistoryTool {
	
	Logger log = Logger.getLogger(this.getClass());
	
	/**
	 * Initialize the process history
	 * @param history
	 * @param processid
	 * @param script
	 * @return
	 */
	public History initProcessHistory(History history, String processid, String script) {
		
		history.setHistory_id(new RandomString(12).nextString());
		
		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(BaseTool.getCurrentMySQLDatetime());
		
		history.setHistory_input(script);
		
		return history;
		
	}
	
	/**
	 * Update or finalize the history in database
	 * @param history
	 */
	public void saveHistory(History history) {
		
    	try {
    		
    		log.info("save history status: " + history.getIndicator());
    		
    		history.setHistory_end_time(BaseTool.getCurrentMySQLDatetime());
    		
    		String logs = history.getHistory_output();
    		
    		//the log is more than 65500 characters, write it into a log file
    		if(logs.length()>65500) {
    			
    			String logfile = SysDir.upload_file_path + "/" + history.getHistory_id() + ".log";
    			
    			BaseTool.writeString2File(logs, BaseTool.getWebAppRootPath() + logfile);
    			
    			history.setHistory_output("logfile");
    			
    		}else {
    			
    			history.setHistory_output(logs);
    			
    		}
    		
    		StringBuffer sql = new StringBuffer("select id from history where id = '").append(history.getHistory_id()).append("'; ");
    		
    		ResultSet rs = DataBaseOperation.query(sql.toString());
    		
			if(!rs.next()) {
				
				sql = new StringBuffer("insert into history (id, process, begin_time, input, output, host, indicator) values ('");
				
				sql.append(history.getHistory_id()).append("','");
				
				sql.append(history.getHistory_process()).append("','");
				
				sql.append(history.getHistory_begin_time()).append("', ?, ?, '");
				
				sql.append(history.getHost_id()).append("', '");
				
				sql.append(history.getIndicator()).append("' )");
				
				DataBaseOperation.preexecute(sql.toString(), new String[] {history.getHistory_input(), history.getHistory_output()});
				
			}else {
				
				sql = new StringBuffer("update history set end_time = '");
				
				sql.append(history.getHistory_end_time());
				
				sql.append("', output = ?, indicator = '").append(history.getIndicator()).append("' where id = '");
				
				sql.append(history.getHistory_id()).append("';");
				
				DataBaseOperation.preexecute(sql.toString(), new String[] {history.getHistory_output()});
				
			}
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
    	
	}
	
	Logger logger = Logger.getLogger(this.getClass());
	
	public String escape_jupyter(String code) {		
		
		return null;
		
	}

	/**
	 * Escape the code text
	 * @param code
	 * @return
	 */
	public String escape(String code) {
		
		String resp = null;
		
		if(!BaseTool.isNull(code))
		
			resp = code.replaceAll("\\\\", "\\\\\\\\")
					.replaceAll("\"", "\\\\\"")
					.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>")
					.replaceAll("	", "\\\\t");
		
//		logger.info(resp);
		
		return resp;
		
	}
	
	public String unescape(String code) {
		
		String resp = code.replaceAll("-.-", "/").replaceAll("-·-", "'").replaceAll("-··-", "\"").replaceAll("->-", "\\n").replaceAll("-!-", "\\r");
		
		logger.info(resp);
		
		return resp;
		
	}
	
	/**
	 * Get all history of a workflow
	 * @param workflow_id
	 * @return
	 */
	public String workflow_all_history(String workflow_id) {
		
		StringBuffer resp = new StringBuffer() ;
		
		StringBuffer sql = new StringBuffer("select * from history where process = '").append(workflow_id).append("'  ORDER BY begin_time DESC;");
		
		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			while(rs.next()) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
				
				resp.append("\"begin_time\": \"").append(rs.getString("begin_time")).append("\", ");
				
				resp.append("\"end_time\": \"").append(rs.getString("end_time")).append("\", ");
				
				resp.append("\"status\": \"").append(ProcessTool.escape(rs.getString("indicator"))).append("\", ");
				
				resp.append("\"output\": \"").append(rs.getString("output")).append("\"}");
				
				num++;
				
			}
			
			resp.append("]");
			
			if(num==0)
				
				resp = new StringBuffer();
			
		} catch (SQLException e) {
			
			e.printStackTrace();
			
		}finally {
			
			DataBaseOperation.closeConnection();
			
		}
		
		return resp.toString();
		
	}
	
	public String process_all_history(String pid) {
		
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

	/**
	 * Stop the process and change the status to stopped
	 * @param history_id
	 */
	public void stop(String history_id) {
		
		try {
			
			SSHSession session = GeoweaverController.sessionManager.sshSessionByToken.get(history_id);
			
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
