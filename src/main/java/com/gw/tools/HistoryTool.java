package com.gw.tools;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.DataBaseOperation;
import com.gw.database.HistoryRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.utils.SysDir;
import com.gw.web.GeoweaverController;
/**
 * 
 * All the actions related to the History table
 * 
 * @author JensenSun
 *
 */
@Service
@Configurable
public class HistoryTool {
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Value("${geoweaver.upload_file_path}")
	String upload_file_path;
	
	@Autowired
	BaseTool bt;
	

	public HistoryTool() {
		
		
	}
	
	public String toJSON(History his) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writeValueAsString(his);
            logger.debug("ResultingJSONstring = " + json);
            //System.out.println(json);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}
	
	
	/**
	 * Initialize the process history
	 * @param history
	 * @param processid
	 * @param script
	 * @return
	 */
	public History initProcessHistory(String processid, String script) {
		
		History history = new History();
		
		history.setHistory_id(new RandomString(12).nextString());
		
		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(bt.getCurrentSQLDate());
		
		history.setHistory_input(script);
		
		return history;
		
	}
	
	public History getHistoryById(String hid) {
		
		History h;
		
		Optional<History> ho = historyrepository.findById(hid);
		
		if(ho.isPresent()) {
			
			h = ho.get();
			
		}else {
			
			h = new History();
			
			h.setHistory_id(hid);
			
		}
		
		return h;
		
	}
	
	/**
	 * Update or finalize the history in database
	 * @param history
	 */
	public void saveHistory(History history) {
		
		historyrepository.save(history);
		
//    	try {
//    		
//    		log.info("save history status: " + history.getIndicator());
//    		
//    		history.setHistory_end_time(bt.getCurrentSQLDate());
//    		
//    		String logs = history.getHistory_output();
//    		
//    		//the log is more than 65500 characters, write it into a log file
//    		if(logs.length()>65500) {
//    			
//    			String logfile = upload_file_path + "/" + history.getHistory_id() + ".log";
//    			
//    			bt.writeString2File(logs, bt.getWebAppRootPath() + logfile);
//    			
//    			history.setHistory_output("logfile");
//    			
//    		}else {
//    			
//    			history.setHistory_output(logs);
//    			
//    		}
//    		
//    		StringBuffer sql = new StringBuffer("select id from history where id = '").append(history.getHistory_id()).append("'; ");
//    		
//    		ResultSet rs = DataBaseOperation.query(sql.toString());
//    		
//			if(!rs.next()) {
//				
//				sql = new StringBuffer("insert into history (id, process, begin_time, input, output, host, indicator) values ('");
//				
//				sql.append(history.getHistory_id()).append("','");
//				
//				sql.append(history.getHistory_process()).append("','");
//				
//				sql.append(history.getHistory_begin_time()).append("', ?, ?, '");
//				
//				sql.append(history.getHost_id()).append("', '");
//				
//				sql.append(history.getIndicator()).append("' )");
//				
//				DataBaseOperation.preexecute(sql.toString(), new String[] {history.getHistory_input(), history.getHistory_output()});
//				
//			}else {
//				
//				sql = new StringBuffer("update history set end_time = '");
//				
//				sql.append(history.getHistory_end_time());
//				
//				sql.append("', output = ?, indicator = '").append(history.getIndicator()).append("' where id = '");
//				
//				sql.append(history.getHistory_id()).append("';");
//				
//				DataBaseOperation.preexecute(sql.toString(), new String[] {history.getHistory_output()});
//				
//			}
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}finally {
//			
//			DataBaseOperation.closeConnection();
//			
//		}
    	
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
		
		if(!bt.isNull(code))
		
			resp = code.replaceAll("\\\\", "\\\\\\\\")
					.replaceAll("\"", "\\\\\"")
					.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>")
					.replaceAll("	", "\\\\t");
		
//		logger.info(resp);
		
		return resp;
		
	}
	
	public String unescape(String code) {
		
		String resp = code.replaceAll("-.-", "/").replaceAll("-·-", "'").replaceAll("-··-", "\"").replaceAll("->-", "\\n").replaceAll("-!-", "\\r");
		
		logger.debug(resp);
		
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
		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		try {
//			
//			resp.append("[");
//			
//			int num = 0;
//			
//			while(rs.next()) {
//				
//				if(num!=0) {
//					
//					resp.append(", ");
//					
//				}
//				
//				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
//				
//				resp.append("\"begin_time\": \"").append(rs.getString("begin_time")).append("\", ");
//				
//				resp.append("\"end_time\": \"").append(rs.getString("end_time")).append("\", ");
//				
//				resp.append("\"status\": \"").append(pt.escape(rs.getString("indicator"))).append("\", ");
//				
//				resp.append("\"output\": \"").append(rs.getString("output")).append("\"}");
//				
//				num++;
//				
//			}
//			
//			resp.append("]");
//			
//			if(num==0)
//				
//				resp = new StringBuffer();
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}finally {
//			
//			DataBaseOperation.closeConnection();
//			
//		}
		
		return resp.toString();
		
	}
	
	public String process_all_history(String pid) {
		
		StringBuffer resp = new StringBuffer();
		
		List<History> active_processes = historyrepository.findByProcessId(pid);
		
//		StringBuffer sql = new StringBuffer("select * from history where indicator='Running'  ORDER BY begin_time DESC;");
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			for(;num<active_processes.size();num++) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				History h = active_processes.get(num);
				
				resp.append("{ \"id\": \"").append(h.getHistory_id()).append("\", ");
				
				resp.append("\"begin_time\": \"").append(h.getHistory_begin_time());
				
				resp.append("\", \"end_time\": \"").append(h.getHistory_end_time());
				
				resp.append("\", \"output\": \"").append(escape(String.valueOf(h.getHistory_output())));
				
				resp.append("\", \"status\": \"").append(escape(String.valueOf(h.getIndicator())));
				
				resp.append("\", \"host\": \"").append(escape(String.valueOf(h.getHost_id())));
				
				resp.append("\"}");
				
			}
			
			resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
//		StringBuffer resp = new StringBuffer() ;
//		
//		StringBuffer sql = new StringBuffer("select * from history where process = '").append(pid).append("'  ORDER BY begin_time DESC;");
		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		try {
//			
//			resp.append("[");
//			
//			int num = 0;
//			
//			while(rs.next()) {
//				
//				if(num!=0) {
//					
//					resp.append(", ");
//					
//				}
//				
//				resp.append("{ \"id\": \"").append(rs.getString("id")).append("\", ");
//				
//				resp.append("\"begin_time\": \"").append(rs.getString("begin_time"));
//				
//				resp.append("\", \"end_time\": \"").append(rs.getString("end_time"));
//				
//				resp.append("\", \"output\": \"").append(escape(rs.getString("output")));
//				
//				resp.append("\", \"status\": \"").append(escape(rs.getString("indicator")));
//				
//				resp.append("\", \"host\": \"").append(escape(rs.getString("host")));
//				
//				resp.append("\"}");
//				
//				num++;
//				
//			}
//			
//			resp.append("]");
//			
//			if(num==0)
//				
//				resp = new StringBuffer();
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}
		
//		return resp.toString();
		
	}
	
	/**
	 * Save Jupyter Notebook Checkpoints into the GW database
	 */
	public void saveJupyterCheckpoints(String hostid, String jupyterbody, HttpHeaders headers) {
		
		try {
			
			History h = new History();
			
			h.setHistory_id(new RandomString(12).nextString());
			
			h.setHistory_begin_time(bt.getCurrentSQLDate());
			
			h.setHistory_input(headers.get("referer").get(0));
			
			h.setHistory_output(jupyterbody);
			
			h.setHost_id(hostid);
			
			h.setIndicator(ExecutionStatus.DONE);
			
			h.setHistory_process(headers.get("referer").get(0));
			
			historyrepository.save(h);
			
			
//			StringBuffer sql = new StringBuffer("insert into history (id, process, begin_time, input, output, host, indicator) values ('");
//			
//			sql.append(new RandomString(12).nextString()).append("','NA','");
//			
//			sql.append(bt.getCurrentMySQLDatetime()).append("', '").append(headers.get("referer").get(0)).append("', ?, '");
//			
//			sql.append(hostid).append("', '");
//			
//			sql.append(ExecutionStatus.DONE).append("' )");
			
//			DataBaseOperation.preexecute(sql.toString(), new String[] {jupyterbody});
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		
	}
	
	public String deleteById(String history_id) {
		
		historyrepository.deleteById(history_id);
		
		return "done";
		
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
			
			String history_end_time = bt.getCurrentMySQLDatetime();

			StringBuffer sql = new StringBuffer("update history set end_time = '");
			
			sql.append(history_end_time);
			
			sql.append("', indicator = 'Stopped' where id = '");
			
			sql.append(history_id).append("';");
			
//			DataBaseOperation.execute(sql.toString());
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
	}

}
