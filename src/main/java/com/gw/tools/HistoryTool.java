package com.gw.tools;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.HistoryRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.web.GeoweaverController;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
/**
 * 
 * All the actions related to the History table
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
@Configurable
public class HistoryTool {
	
	Logger log = Logger.getLogger(this.getClass());
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Value("${geoweaver.upload_file_path}")
	String upload_file_path;
	
	@Autowired
	BaseTool bt;

	// @Autowired
	// ProcessTool pt;
	

	public HistoryTool() {
		
		
	}
	
	public String toJSON(History his) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(his);
            // logger.debug("ResultingJSONstring = " + json);
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
	public History initProcessHistory(String history_id, String processid, String script) {
		
		History history = new History();
		
		history.setHistory_id(history_id);
		
		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		history.setHistory_begin_time(BaseTool.getCurrentSQLDate());
		
		history.setHistory_input(script);
		
		return history;
		
	}

	public String getWorkflowProcessHistory(String workflowhistoryid, String processid){

		History h = this.getHistoryById(workflowhistoryid);

		String[] processes = h.getHistory_input().split(";");

		String[] processhistories = h.getHistory_output().split(";");

		if(processes.length==processhistories.length){

			h = null;

			for(int i=0;i<processes.length;i++){

				if(processes[i].equals(processid)){

					h = this.getHistoryById(processhistories[i]);

					break;

				}

			}

		}

		return this.toJSON(h);

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

		if(BaseTool.isNull(history.getIndicator())){

			logger.warn("This indicator shouldn't be null in all scenarios");

		}

		synchronized(historyrepository){

			historyrepository.saveAndFlush(history);
		
		}
    	
	}
	
	Logger logger = Logger.getLogger(this.getClass());
	
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
		
		logger.debug(resp);
		
		return resp;
		
	}

	public List<History> getHistoryByWorkflowId(String wid){

		return historyrepository.findByProcessId(wid);

	}
	
	/**
	 * Get all history of a workflow
	 * @param workflow_id
	 * @return
	 */
	public String workflow_all_history(String workflow_id) {
		
		StringBuffer resp = new StringBuffer() ;
		
		List<History> active_processes = historyrepository.findByProcessId(workflow_id);
		
		try {

			String json = "[]";
			ObjectMapper mapper = new ObjectMapper();
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(active_processes);

			resp.append(json);
			
			// resp.append("[");
			
			// int num = 0;
			
			// for(;num<active_processes.size();num++) {
				
			// 	if(num!=0) {
					
			// 		resp.append(", ");
					
			// 	}
				
			// 	History h = active_processes.get(num);
				
			// 	resp.append("{ \"id\": \"").append(h.getHistory_id()).append("\", ");
				
			// 	resp.append("\"begin_time\": \"").append(h.getHistory_begin_time());
				
			// 	resp.append("\", \"end_time\": \"").append(h.getHistory_end_time());
				
			// 	resp.append("\", \"output\": \"").append(bt.escape(String.valueOf(h.getHistory_output())));
				
			// 	resp.append("\", \"status\": \"").append(bt.escape(String.valueOf(h.getIndicator())));
				
			// 	resp.append("\", \"host\": \"").append(bt.escape(String.valueOf(h.getHost_id())));
				
			// 	resp.append("\"}");
				
			// }
			
			// resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	public String process_all_history(String pid) {
		
		StringBuffer resp = new StringBuffer();
		
		List<History> active_processes = historyrepository.findByProcessId(pid);
		
		try {

			String json = "[]";
			ObjectMapper mapper = new ObjectMapper();
			json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(active_processes);

			resp.append(json);
			
			// resp.append("[");
			
			// int num = 0;
			
			// for(;num<active_processes.size();num++) {
				
			// 	if(num!=0) {
					
			// 		resp.append(", ");
					
			// 	}
				
			// 	History h = active_processes.get(num);
				
			// 	resp.append("{ \"id\": \"").append(h.getHistory_id()).append("\", ");
				
			// 	resp.append("\"begin_time\": \"").append(h.getHistory_begin_time());
				
			// 	resp.append("\", \"end_time\": \"").append(h.getHistory_end_time());
				
			// 	resp.append("\", \"output\": \"").append(bt.escape(String.valueOf(h.getHistory_output())));
				
			// 	resp.append("\", \"status\": \"").append(bt.escape(String.valueOf(h.getIndicator())));
				
			// 	resp.append("\", \"host\": \"").append(bt.escape(String.valueOf(h.getHost_id())));
				
			// 	resp.append("\"}");
				
			// }
			
			// resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
		
	}

	public String deleteAllHistoryByHost(String hostid){

		String resp = null;

		try{

			Collection<History> historylist = historyrepository.findRecentHistory(hostid, 1000);

			Iterator<History> hisint = historylist.iterator();

			StringBuffer idlist = new StringBuffer();

			while(hisint.hasNext()) {
				
				History h = hisint.next();
				
				idlist.append(h.getHistory_id()).append(",");

				historyrepository.delete(h);

			}

			resp = "{ \"removed_history_ids\": \"" + idlist.toString() + "\"";

		}catch(Exception e){

			e.printStackTrace();

		}

		return resp;

	}

	public String deleteNoNotesHistoryByHost(String hostid){

		String resp = null;

		try{

			Collection<History> historylist = historyrepository.findRecentHistory(hostid, 1000);

			Iterator<History> hisint = historylist.iterator();

			StringBuffer idlist = new StringBuffer();

			while(hisint.hasNext()) {
				
				History h = hisint.next();

				if(BaseTool.isNull(h.getHistory_notes())){

					idlist.append(h.getHistory_id()).append(",");

					historyrepository.delete(h);

				}
				
			}

			resp = "{ \"removed_history_ids\": \"" + idlist.toString() + "\"";

		}catch(Exception e){

			e.printStackTrace();

		}

		return resp;

	}
	
	/**
	 * Update the notes of a history
	 */
	public void updateNotes(String hisid, String notes){

		try{

			logger.info("Updating history: " + hisid + " - " + notes);

			History h = this.getHistoryById(hisid);

			h.setHistory_notes(notes);

			this.saveHistory(h);

		}catch(Exception e){

			e.printStackTrace();

		}

	}

	public boolean checkIfEnd(History hist){

		if(ExecutionStatus.FAILED.equals(hist.getIndicator()) || 
			ExecutionStatus.STOPPED.equals(hist.getIndicator()) ||
			ExecutionStatus.DONE.equals(hist.getIndicator()) ||
			ExecutionStatus.UNKOWN.equals(hist.getIndicator()))
				return true;
		else
				return false;
                

	}

	/**
	 * Save Jupyter Notebook Checkpoints into the GW database
	 */
	public void saveJupyterCheckpoints(String hostid, String jupyterbody, HttpHeaders headers) {
		
		try {
			
			History h = new History();
			
			h.setHistory_id(new RandomString(12).nextString());
			
			h.setHistory_begin_time(BaseTool.getCurrentSQLDate());
			
			h.setHistory_input(headers.get("referer").get(0));
			
			h.setHistory_output(jupyterbody);
			
			h.setHost_id(hostid);
			
			h.setIndicator(ExecutionStatus.DONE);
			
			h.setHistory_process(headers.get("referer").get(0));
			
			historyrepository.save(h);
			
			
			
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

			if(historyrepository.findById(history_id).isPresent()){

				History oldh = historyrepository.findById(history_id).get();
			
				oldh.setHistory_end_time(BaseTool.getCurrentSQLDate());

				oldh.setIndicator("Stopped");

				historyrepository.save(oldh);

			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
		
	}

}
