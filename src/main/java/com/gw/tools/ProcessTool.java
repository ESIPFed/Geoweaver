package com.gw.tools;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.local.LocalSession;
import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.web.GeoweaverController;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("prototype")
public class ProcessTool {
	
	Logger logger = LoggerFactory.getLogger(ProcessTool.class);
	
	@Autowired
	HistoryTool history_tool;
	
	@Autowired
	ProcessRepository processrepository;
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Autowired
	BaseTool bt;

	@Value("${geoweaver.workspace}")
	String workspace;
	
	@Value("${geoweaver.upload_file_path}")
	String upload_file_path;
	
	public ProcessTool() {
		
		
	}

	public void save(GWProcess p){

		processrepository.save(p);

	}
	
	public List<GWProcess> getAllProcesses(){

		Iterable<GWProcess> pit = processrepository.findAll();

		List<GWProcess> plist = new ArrayList();

		pit.forEach(p->{

			plist.add(p);

		});

		return plist;

	}
	
	public String toJSON(GWProcess p) {
		
		String json = "{}";
        ObjectMapper mapper = new ObjectMapper();
        try {
            json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
		return json;
		
	}
	
	/**
	 * Get the list of processes
	 * @param owner
	 * @param isactive
	 * If this is true, only return the processes that are still running.
	 * @return
	 * @throws SQLException
	 */
	public String list(String owner) throws SQLException {
		
		Iterator<GWProcess> pit = processrepository.findAllPublic().iterator();
		
		StringBuffer json = new StringBuffer("[");
		
		while(pit.hasNext()) {
			
			GWProcess p = pit.next();
			
			json.append(toJSON(p)).append(",");
			
		}

		pit = processrepository.findAllPrivateByOwner(owner).iterator();

		while(pit.hasNext()){

			json.append(toJSON(pit.next())).append(",");

		}

		if(json.length()>1) json.deleteCharAt(json.length() - 1);
		
		json.append("]");
		
		return json.toString();
		
	}
	
	/**
	 * Get Process Object by Process Id
	 * @param id
	 * Process ID
	 * @return
	 * GWProcess
	 */
	public GWProcess getProcessById(String id) {
		
		GWProcess p = processrepository.findById(id).get();
		
		return p;
		
	}
	
	public String escapeJupyter(String code){

		if(!BaseTool.isNull(code) && (code.contains("bash\\\n") || code.contains("\\\nimport") 
			|| code.contains("\\\"operation\\\"") || code.contains("\\\"cells\\\""))){

				code = this.unescape(code);

		}

		return code;
	}

	public String detail(String id) throws JsonProcessingException {
		
		GWProcess p = getProcessById(id);

		if(!BaseTool.isNull(p.getCode()) && (p.getCode().contains("bash\\\n") || p.getCode().contains("\\\nimport") 
			|| p.getCode().contains("\\\"operation\\\"") || p.getCode().contains("\\\"cells\\\""))){

			p.setCode(this.unescape(p.getCode()));

		}

		ObjectMapper mapper = new ObjectMapper();

		String jsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(p);
		
		return jsonString;
		
	}
	
	public String escape_jupyter(String code) {		
		
		return null;
		
	}

	public String getSuffix(String lang){

		String suffix = "";

		if("shell".equals(lang)){

			suffix = "sh";

		}else if("python".equals(lang)){

			suffix = "py";

		}else if("jupyter".equals(lang)){

			suffix = "ipynb";

		}else if("builtin".equals(lang)){

			suffix = "builtin";

		}

		return suffix;
	}

	public String getProcessFileName(String pid){

		GWProcess gp = this.getProcessById(pid);

		String filename = gp.getName() + "." + getSuffix(gp.getLang());

		return filename;

	}
	
	public String unescape(String code) {
		
		String resp = code;
		
		if(!BaseTool.isNull(code)) {
			
			resp = StringEscapeUtils.unescapeJson(code);
			
		}
		
		return resp;
		
	}
	
	/**
	 * Update the Process
	 * @param p
	 * Process Object
	 */
	public void update(GWProcess p ) {
		
		processrepository.save(p);
		
	}

	public GWProcess fromJSON(String json){
		
		GWProcess p = null;
		
		try {

			ObjectMapper mapper = new ObjectMapper();

			p = mapper.readValue(json, GWProcess.class);
		
		} catch (Exception e) {
		
			e.printStackTrace();
		
		}
		
		return p;
	}

	/**
	 * for jupyter, save the jupyter nbconvert to replace the code
	 * @param h
	 * @param token
	 */
	public void updateJupyter(History h, String token) {
		
		if(h.getIndicator().equals(ExecutionStatus.DONE)) {
			
			GWProcess p = getProcessById(h.getHistory_process());
			
			if(!BaseTool.isNull(p.getDescription())&&p.getDescription().equals("jupyter")) {
				
				String newfilename = p.getName();
				
				if(!newfilename.endsWith(".ipynb")) {
					
					newfilename += ".nbconvert.ipynb";
					
				}else {
					
					newfilename = newfilename.replace(".ipynb", ".nbconvert.ipynb");
					
				}
				
				String resfile = workspace + "/" + token + "/" + newfilename;
				
				if(new File(resfile).exists()) {
					
					String newresult = bt.readStringFromFile(resfile);
					
					p.setCode(newresult);
					
					this.update(p);
					
				}
				
			}
			
		}
		
	}
	
	/**
	 * Update the Process
	 * @param id
	 * @param name
	 * @param lang
	 * @param code
	 * @param description
	 */
	public void update(String id, String name, String lang, String code, String description) {
		
		GWProcess p = processrepository.findById(id).get();
		
		p.setName(name);
		
		p.setDescription(lang);
		
		p.setCode(bt.escape(code));
		
		processrepository.save(p);
		
	}
	/**
	 * add the process to the local file
	 * @param name
	 * @param lang
	 * @param code
	 * @param desc
	 * @return
	 */
	public String add_local(String name, String lang, String code, String desc) {
		
		String folderpath = bt.getFileTransferFolder() + "/";
		
		String filename = "jupyter-code-" + new RandomString(7).nextString();
		
		String filepath = folderpath + filename;
		
		bt.writeString2File(code, filepath);
		
		String newid = new RandomString(6).nextString();
		
		GWProcess p = new GWProcess();
		
		p.setId(newid);
		
		p.setCode(code);
		
		p.setName(name);
		
		p.setDescription(desc);
		
		processrepository.save(p);
		
		return newid;
		
	}
	
	/**
	 * add code to database
	 * @param name
	 * @param lang
	 * @param code
	 * @param description
	 * @return
	 */
	public String add_database(String name, String lang, String code, String desc, String ownerid, String confidential) {
		
		GWProcess p = new GWProcess();
		
		p.setCode(bt.escape(code));
		
		p.setDescription(desc);
		
		String newid = new RandomString(6).nextString();
		
		p.setId(newid);
		
		p.setName(name);

		p.setOwner(ownerid);

		p.setConfidential(confidential);
		
		processrepository.save(p);

		return newid;
		
	}
	
	/**
	 * Add local file into database with fixed file location and server id
	 * @param name
	 * @param type
	 * @param code
	 * @param filepath
	 * @param hid
	 * @return
	 */
	public String add_database(String name, String type, String code, String filepath, String hid, String ownerid, String confidential) {
		
		String newid = null;
		
		try {
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
		}
		
		return newid;
		
	}
	
	/**
	 * Add process
	 * @param name
	 * @param lang
	 * @param code
	 * @param desc
	 * @return
	 */
	public String add(String name, String lang, String code, String desc, String ownerid, String confidential) {
		
		String newid = null;
		
		newid = add_database(name, lang, code, desc, ownerid, confidential);
			
		return newid;
		
	}
	
	
	public String del(String id) {
		
		processrepository.deleteById(id);
		
		return "done";
		
	}
	
	/**
	 * Get process name by id
	 * @param pid
	 * @return
	 */
	public String getNameById(String pid) {
		
		GWProcess p = processrepository.findById(pid).get();

		return p.getName();
		
	}
	
	/**
	 * Get code by Id
	 * @param pid
	 * @return
	 */
	public String getCodeById(String pid) {
		
		GWProcess p = processrepository.findById(pid).get();
		
		String code = p.getCode();
		
		if(code.contains("bash\\\n") || code.contains("\\\nimport") 
		|| code.contains("\\\"operation\\\"") || code.contains("\\\"cells\\\"")) 
			code = this.unescape(code);
		
		return code;
		
	}
	
	/**
	 * get category of the process,e.g. shell, python, R, java, geoweaver-builtin, etc. 
	 * @param pid
	 * @return
	 */
	public String getTypeById(String pid) {
		
		GWProcess p = processrepository.findById(pid).get();
		
		return BaseTool.isNull(p.getLang())?p.getDescription():p.getLang();
		
		
	}
	
	
	
	
	/**
	 * For Andrew
	 * @param hisid
	 * @return
	 */
	public String stop(String hisid) {
		
		String resp = null;
		
		try {
			
			//stop remote ssh session
			SSHSession remotesession = GeoweaverController.sessionManager.sshSessionByToken.get(hisid);
			
			if(!BaseTool.isNull(remotesession)) remotesession.getSSHJSession().close();

			//stop local session
			LocalSession localsession = GeoweaverController.sessionManager.localSessionByToken.get(hisid);

			if(!BaseTool.isNull(localsession)) localsession.stop();
			
			history_tool.stop(hisid);
			
			resp = "{\"history_id\": \""+hisid+"\", \"ret\": \"stopped\"}";
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
        		
		return resp;
		
	}

	
	
	public String recent(int limit) {
		
		StringBuffer resp = new StringBuffer();
		
		List<Object[]> recent_processes = historyrepository.findRecentProcess(limit);
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			for(;num<recent_processes.size();num++) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				Object[] process_obj = recent_processes.get(num);
				
				resp.append("{ \"id\": \"").append(process_obj[0]).append("\", ");
				
				resp.append("\"name\": \"").append(process_obj[14]).append("\", ");
				
				resp.append("\"notes\": \"").append(process_obj[4]).append("\", ");
				
				resp.append("\"end_time\": \"").append(process_obj[2]).append("\", ");
				
				resp.append("\"status\": \"").append(process_obj[8]).append("\", ");
				
				resp.append("\"begin_time\": \"").append(process_obj[1]).append("\"}");
				
			}
			
			resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	public String removeClob(String clob) {
		
		int fn = clob.indexOf("STRINGDECODE('");
		
		String substr = clob;
		
		if(fn!=-1) {
			
			fn += 14;
			
			substr = clob.substring(fn);
			
			substr = substr.substring(0, substr.length()-2);
			
			logger.info(substr);
			
		}
		
		return substr;
		
	}
	
	/**
	 * get all details of one history
	 * @param hid
	 * @return
	 */
	public String one_history(String hid) {
		
		StringBuffer resp = new StringBuffer();
		
		try {
			
			Optional<History> resop = historyrepository.findById(hid);

			if(resop.isPresent()){

				History hist = resop.get();

				if(!BaseTool.isNull(hist)) {

					GWProcess thep = processrepository.findById(hist.getHistory_process()).get();
					
					resp.append("{ \"hid\": \"").append(hist.getHistory_id()).append("\", ");
					
					resp.append("\"id\": \"").append(hist.getHistory_process()).append("\", ");
					
					resp.append("\"name\": \"").append(thep.getName()).append("\", ");
					
					resp.append("\"notes\": \"").append(hist.getHistory_notes()).append("\", ");
					
					resp.append("\"begin_time\":\"").append(hist.getHistory_begin_time()).append("\", ");
					
					resp.append("\"end_time\":\"").append(hist.getHistory_end_time()).append("\", ");
					
					String input_code = bt.escape(this.escapeJupyter(hist.getHistory_input()));

					resp.append("\"input\":\"").append(input_code).append("\", ");
					
					String output_code = bt.escape(String.valueOf(hist.getHistory_output()));
					
					resp.append("\"output\":\"").append(output_code).append("\", ");
					
					resp.append("\"lang\":\"").append(thep.getLang()).append("\", ");
					
					resp.append("\"host\":\"").append(hist.getHost_id()).append("\", ");

					resp.append("\"confidential\":\"").append(thep.getConfidential()).append("\", ");
					
					resp.append("\"status\":\"").append(hist.getIndicator()).append("\" }");
					
				}

			}
			
		} catch (Exception e) {
		
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}
	
	
	
	/**
	 * Get all active processes
	 * @return
	 */
	public String all_active_process() {
		
		StringBuffer resp = new StringBuffer();
		
		List<Object[]> active_processes = historyrepository.findRunningProcess();
		
		try {
			
			resp.append("[");
			
			int num = 0;
			
			for(;num<active_processes.size();num++) {
				
				if(num!=0) {
					
					resp.append(", ");
					
				}
				
				Object[] row = active_processes.get(num);
				
				resp.append("{ \"id\": \"").append(row[0]).append("\", ");
				
				resp.append("\"begin_time\": \"").append(row[1]);
				
				resp.append("\", \"end_time\": \"").append(row[2]);
				
				resp.append("\", \"output\": \"").append(bt.escape(String.valueOf(row[3])));
				
				resp.append("\", \"status\": \"").append(bt.escape(String.valueOf(row[4])));
				
				resp.append("\", \"notes\": \"").append(bt.escape(String.valueOf(row[8])));
				
				resp.append("\", \"host\": \"").append(bt.escape(String.valueOf(row[5])));
				
				resp.append("\"}");
				
			}
			
			resp.append("]");
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
		return resp.toString();
		
	}

	/**
	 * get all the execution history of this process
	 * @param pid
	 * @return
	 */
	public String all_history(String pid, boolean ignore_skipped) {
		
		return history_tool.process_all_history(pid, ignore_skipped);
		
	}
	
	public String all_history(String pid) {

		return all_history(pid, true);

	}
	
	
}
