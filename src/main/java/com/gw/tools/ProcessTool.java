package com.gw.tools;

import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.ssh.SSHSession;
import com.gw.tasks.GeoweaverProcessTask;
import com.gw.tasks.TaskManager;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.web.GeoweaverController;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProcessTool {
	
	Logger logger = LoggerFactory.getLogger(ProcessTool.class);
	
	@Autowired
	HistoryTool history_tool;
	
	@Autowired
	ProcessRepository processrepository;
	
	@Autowired
	HistoryRepository historyrepository;
	
	@Autowired
	HostTool ht;
	
	@Autowired
	LocalhostTool lt;
	
	@Autowired
	RemotehostTool rt;
	
	@Autowired
	BaseTool bt;

	@Autowired
	GeoweaverProcessTask process_task;

	@Autowired
	TaskManager tm;
	
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
            json = mapper.writeValueAsString(p);
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
		
//		history_tool.process_all_history(pid)
		
		// Iterator<GWProcess> pit = processrepository.findAll().iterator();

		Iterator<GWProcess> pit = processrepository.findAllPublicPrivateByOwner(owner).iterator();
		
		StringBuffer json = new StringBuffer("[");
		
		int num = 0;
		
		while(pit.hasNext()) {
			
			GWProcess p = pit.next();
			
			if( num++ != 0) {
				
				json.append(",");
				
			}
			
			json.append(toJSON(p));
			
		}
		
		json.append("]");
		
		return json.toString();
		
//		StringBuffer json = new StringBuffer("[");
//		
//		StringBuffer sql = new StringBuffer("select id, name, description from process_type");
//		
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		int num = 0;
//		
//		while(rs.next()) {
//			
//			String id = rs.getString("id");
//			
//			String name = rs.getString("name");
//			
//			String desc = rs.getString("description");
//			
//			if(BaseTool.isNull(desc)) desc = "shell";
//			
//			if(!StringUtils.isNumeric(id)) {
//				
//				if(num!=0) {
//					
//					json.append(",");
//					
//				}
//				
//				json.append("{\"id\": \"").append(id).append("\", \"name\": \"")
//					
//					.append(name).append("\", \"desc\":\"").append(desc).append("\"}");
//
//				num++;
//				
//			}
//			
//		}
//		
//		json.append("]");
//		
//		DataBaseOperation.closeConnection();
//		
//		return json.toString();
		
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
		
//		GWProcess p = new GWProcess();
//		
//		StringBuffer sql = new StringBuffer("select * from process_type where id = '").append(id).append("';");
//		
////		StringBuffer resp = new StringBuffer();
//		
//		try {
//
//			ResultSet rs = DataBaseOperation.query(sql.toString());
//			
//			if(rs.next()) {
//				
//				p.setId(rs.getString("id"));
//				
//				p.setName(rs.getString("name"));
//				
//				String lang = "shell";
//				
//				if(!BaseTool.isNull(rs.getString("description")))
//				
//					lang = rs.getString("description");
//				
//				p.setDescription(lang);
//				
//				String code = rs.getString("code");
//
//				if(lang.equals("jupyter")) {
//					
//				}else {
//					
//					code = escape(code);
//					
//				}
//				
//				p.setCode(code);
//				
//			}
//			
//		} catch (Exception e) {
//			
//			e.printStackTrace();
//			
//			throw new RuntimeException(e.getLocalizedMessage());
//			
//		}
		
		return p;
		
	}
	
	public String detail(String id) throws JsonProcessingException {
		
		GWProcess p = getProcessById(id);

		ObjectMapper mapper = new ObjectMapper();

		String jsonString = mapper.writeValueAsString(p);
		
// 		StringBuffer resp = new StringBuffer();
		
// 		resp.append("{ \"id\":\"").append(p.getId()).append("\", ");
		
// 		resp.append("\"name\":\"").append(p.getName()).append("\", ");
		
// 		String lang = "shell";
		
// 		if(bt.isNull(p.getLang())){

// 			if(!bt.isNull(p.getDescription()))
		
// 			lang = p.getDescription();
// 		}
		
// 		resp.append("\"lang\":\"").append(lang).append("\", ");

// 		resp.append("\"description\":\"").append(p.getDescription()).append("\", ");
		
// 		String code = p.getCode();

// //		if(lang.equals("jupyter")) {
// //			
// //			resp.append("\"code\":").append(code).append(" ");
// //			
// //		}else {
			
// //			code = escape(code); //it already escaped once
			
// 			resp.append("\"code\":\"").append(code).append("\", ");

// 			resp.append("\"owner\":\"").append(p.getOwner()).append("\",");

// 			resp.append("\"confidential\":\"").append(p.getConfidential()).append("\"");
			
// //		}
		
// 		resp.append(" }");
				
		return jsonString;
		
	}
	
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
		
		if(!bt.isNull(code)) {

			// resp = code.replaceAll("\\\\", "\\\\\\\\")
			// 		.replaceAll("\"", "\\\\\"")
			// 		.replaceAll("(\r\n|\r|\n|\n\r)", "<br/>")
			// 		.replaceAll("	", "\\\\t");
			resp = StringEscapeUtils.escapeJson(code);
			
		}
			
		return resp;
		
	}
	
	public String unescape(String code) {
		
		String resp = code;
		
		if(!bt.isNull(code)) {
			
			// resp = code.replace("\\\\", "\\")
			// 		.replace("\\\"", "\"")
			// 		.replace("<br/>", "\n")
			// 		.replace("\t", "	");
			resp = StringEscapeUtils.unescapeJson(code);
			
		}
		
		
		
//		String resp = code.replaceAll("-.-", "/").replaceAll("-·-", "'").replaceAll("-··-", "\"").replaceAll("->-", "\\n").replaceAll("-!-", "\\r");
//		
//		logger.info(resp);
		
		return resp;
		
	}
	
	/**
	 * Update the Process
	 * @param p
	 * Process Object
	 */
	public void update(GWProcess p ) {
		
//		logger.info("The process code is updated: " + p.getCode());
		
//		StringBuffer sql = new StringBuffer("update process_type set name = '").append(p.getName())
//				
//				.append("', code = ?, description = '").append(p.getDescription()).append("' where id = '").append(p.getId()).append("';");
//		
////		logger.info(sql.toString());
//		
//		DataBaseOperation.preexecute(sql.toString(), new String[] {p.getCode()});
		
		processrepository.save(p);
		
	}

	/**
	 * for jupyter, save the jupyter nbconvert to replace the code
	 * @param h
	 * @param token
	 */
	public void updateJupyter(History h, String token) {
		
		if(h.getIndicator().equals(ExecutionStatus.DONE)) {
			
			GWProcess p = getProcessById(h.getHistory_process());
			
			if(!bt.isNull(p.getDescription())&&p.getDescription().equals("jupyter")) {
				
//								log.info("save new jupyter into the code");
				
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
		
//		StringBuffer sql = new StringBuffer("update process_type set name = '").append(name)
//				
//				.append("', code = ?, description = '").append(lang).append("' where id = '").append(id).append("';");
//		
//		logger.info(sql.toString());
//		
//		DataBaseOperation.preexecute(sql.toString(), new String[] {code});
		
		GWProcess p = processrepository.findById(id).get();
		
		p.setName(name);
		
		p.setDescription(lang);
		
		p.setCode(escape(code));
		
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
		
//		StringBuffer sql = new StringBuffer("insert into process_type (id, name, code, description) values ('");
//		
//		sql.append(newid).append("', '");
//		
//		sql.append(name).append("', ?, '");
//		
//		sql.append(desc).append("'); ");
//		
//		logger.info(sql.toString());
//		
//		DataBaseOperation.preexecute(sql.toString(), new String[] {filename});
		
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
		
		p.setCode(this.escape(code));
		
		p.setDescription(desc);
		
		String newid = new RandomString(6).nextString();
		
		p.setId(newid);
		
		p.setName(name);

		p.setOwner(ownerid);

		p.setConfidential(confidential);
		
		processrepository.save(p);

//		
//		
//		StringBuffer sql = new StringBuffer("insert into process_type (id, name, code, description) values ('");
//		
//		sql.append(newid).append("', '");
//		
//		sql.append(name).append("', ?, '");
//		
//		sql.append(desc).append("'); ");
//		
//		logger.info(sql.toString());
//		
//		DataBaseOperation.preexecute(sql.toString(), new String[] {code});
		
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
			
////			check if the file is already in the database. If yes, should replace the process content only instead of inserting a new row.
//			StringBuffer sql = new StringBuffer("select * from process_type where inputs = '")
//					.append(filepath).append("' and inputs_datatypes = '").append(hid).append("'; ");
//			
//			ResultSet rs = DataBaseOperation.query(sql.toString());
//			
//			
//			
//			if(rs.next()) {
//				
//				sql = new StringBuffer("update process_type set code = ? where inputs = '")
//					.append(filepath).append("' and inputs_datatypes = '").append(hid).append("'; ");
//				
//				logger.info(sql.toString());
//				
//				DataBaseOperation.preexecute(sql.toString(), new String[] {code});
//				
//			}else {
//				
//				newid = new RandomString(6).nextString();
//				
//				sql = new StringBuffer("insert into process_type (id, name, code, description, inputs, inputs_datatypes) values ('");
//				
//				sql.append(newid).append("', '");
//				
//				sql.append(name).append("', ?, '");
//				
//				sql.append(type).append("', '");
//				
//				sql.append(filepath).append("', '");
//				
//				sql.append(hid).append("'); ");
//				
//				logger.info(sql.toString());
//				
//				DataBaseOperation.preexecute(sql.toString(), new String[] {code});
//				
//			}
			
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
		
//		if(lang.equals("jupyter")) {
//			
//			newid = ProcessTool.add_database(name, lang, code, desc); //jupyter still goes to the database
//			
//		}else {
			
			newid = add_database(name, lang, code, desc, ownerid, confidential);
			
//		}
		
		return newid;
		
	}
	
	
	public String del(String id) {
		
//		StringBuffer sql = new StringBuffer("delete from process_type where id = '").append(id).append("';");
//		
//		DataBaseOperation.execute(sql.toString());
		
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

//		StringBuffer sql = new StringBuffer("select name from process_type where id = '").append(pid).append("';");
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		String name = null;
//		
//		try {
//			
//			if(rs.next()) {
//				
//				name = rs.getString("name");
//				
//			}
//			
//			DataBaseOperation.closeConnection();
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}
		
		return p.getName();
		
	}
	
	/**
	 * Get code by Id
	 * @param pid
	 * @return
	 */
	public String getCodeById(String pid) {
		
//		StringBuffer sql = new StringBuffer("select name,code from process_type where id = '").append(pid).append("';");
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		String code = null;
//		
//		try {
//			
//			if(rs.next()) {
//				
//				code = rs.getString("code");
//				
//			}
//			
//			DataBaseOperation.closeConnection();
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}
		
		GWProcess p = processrepository.findById(pid).get();
		
		String code = p.getCode();
		
		code = this.unescape(code);
		
		return code;
		
	}
	
	/**
	 * get category of the process,e.g. shell, python, R, java, geoweaver-builtin, etc. 
	 * @param pid
	 * @return
	 */
	public String getTypeById(String pid) {
		
//		StringBuffer sql = new StringBuffer("select description from process_type where id = '").append(pid).append("';");
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
//		
//		String desc = null;
//		
//		try {
//			
//			if(rs.next()) {
//				
//				desc = rs.getString("description");
//				
//			}
//			
//			DataBaseOperation.closeConnection();
//			
//			if(BaseTool.isNull(desc)) desc = "shell"; //default shell
//			
//		} catch (SQLException e) {
//			
//			e.printStackTrace();
//			
//		}
		GWProcess p = processrepository.findById(pid).get();
		
		
		return p.getDescription();
		
		
	}
	
	
	
	
	/**
	 * For Andrew
	 * @param hisid
	 * @return
	 */
	public String stop(String hisid) {
		
		String resp = null;
		
		try {
			
			SSHSession session = GeoweaverController.sessionManager.sshSessionByToken.get(hisid);
			
			if(!bt.isNull(session))
				
				session.getSSHJSession().close();
			
			//establish SSH session and generate a token for it
//			
//			if(token == null) {
//				
//				token = new RandomString(12).nextString();
//				
//			}
//			
//			SSHSession session = new SSHSessionImpl();
//			
//			session.login(hid, pswd, token, false);
//			
//			GeoweaverController.sshSessionManager.sshSessionByToken.put(token, session);
//			
//			String code = "#!/bin/bash\n" + 
//					"kill -9 " + hid;
//			
//			session.runBash(code, id, isjoin); 
//			
			history_tool.stop(hisid);
//				
			resp = "{\"history_id\": \""+hisid+
//					
//					"\", \"token\": \""+token+
//					
					"\", \"ret\": \"stopped\"}";
			
//			SSHCmdSessionOutput task = new SSHCmdSessionOutput(code);
			
			//register the input/output into the database
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			throw new RuntimeException(e.getLocalizedMessage());
			
		}
        		
		return resp;
		
	}

	/**
	 * Execute the process using workers
	 * This should be the method called by the controller
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param httpsessionid
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @return
	 */
	public String executeByWorker(String history_id, String id, String hid, String pswd, String httpsessionid, 
			boolean isjoin, String bin, String pyenv, String basedir) {

			process_task.initialize(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir,null);
			tm.addANewTask(process_task);

			return null;
	
	}
	
	/**
	 * Execute the process directly
	 * This method should be only called by a worker
	 * @param id
	 * @param hid
	 * @param pswd
	 * @param httpsessionid
	 * @param isjoin
	 * @param bin
	 * @param pyenv
	 * @param basedir
	 * @return
	 */
	public String execute(String history_id, String id, String hid, String pswd, String httpsessionid, 
			boolean isjoin, String bin, String pyenv, String basedir) {

		
		String category = getTypeById(id);
		
		logger.debug("this process is : " + category);
		
		String resp = null;
		
		if(ht.islocal(hid)) {
			
			//localhost
			if("shell".equals(category)) {
				
				resp = lt.executeShell(history_id, id, hid, pswd, httpsessionid, isjoin);
				
			}else if("builtin".equals(category)) {
				
				resp = lt.executeBuiltInProcess(history_id, id, hid, pswd, httpsessionid, isjoin);
				
			}else if("jupyter".equals(category)){
				
				resp = lt.executeJupyterProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
				
			}else if("python".equals(category)) {
				
				resp = lt.executePythonProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
				
			}else{
				
				throw new RuntimeException("This category of process is not supported");
				
			}
			
			
		}else {
			
			//non-local remote server

			if("shell".equals(category)) {
				
				resp = rt.executeShell(history_id, id, hid, pswd, httpsessionid, isjoin);
				
			}else if("builtin".equals(category)) {
				
				resp = rt.executeBuiltInProcess(history_id, id, hid, pswd, httpsessionid, isjoin);
				
			}else if("jupyter".equals(category)){
				
				resp = rt.executeJupyterProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
				
			}else if("python".equals(category)) {
				
				resp = rt.executePythonProcess(history_id, id, hid, pswd, httpsessionid, isjoin, bin, pyenv, basedir);
				
			}else{
				
				throw new RuntimeException("This category of process is not supported");
				
			}

			
		}
		
		return resp;
		
	}

	public String recent(int limit) {
		
		StringBuffer resp = new StringBuffer();
		
//		StringBuffer sql = new StringBuffer("select * from history, process_type where process_type.id = history.process ORDER BY begin_time DESC limit ")
//				.append(limit).append(";");
//		
//		ResultSet rs = DataBaseOperation.query(sql.toString());
		
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
				
				resp.append("\"name\": \"").append(process_obj[12]).append("\", ");
				
				resp.append("\"notes\": \"").append(process_obj[8]).append("\", ");
				
				resp.append("\"end_time\": \"").append(process_obj[2]).append("\", ");
				
				resp.append("\"status\": \"").append(process_obj[7]).append("\", ");
				
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
		
//		StringBuffer sql = new StringBuffer("select * from history, process_type where history.id = '").append(hid).append("' and history.process=process_type.id;");
		
//		logger.info(sql.toString());
		
		try {
			
//			ResultSet rs = DataBaseOperation.query(sql.toString());

			History hist = historyrepository.findById(hid).get();

			if(!bt.isNull(hist)) {

				GWProcess thep = processrepository.findById(hist.getHistory_process()).get();
				
				resp.append("{ \"hid\": \"").append(hist.getHistory_id()).append("\", ");
				
				resp.append("\"id\": \"").append(hist.getHistory_process()).append("\", ");
				
				resp.append("\"name\": \"").append(thep.getName()).append("\", ");
				
				resp.append("\"notes\": \"").append(hist.getHistory_notes()).append("\", ");
				
				resp.append("\"begin_time\":\"").append(hist.getHistory_begin_time()).append("\", ");
				
				resp.append("\"end_time\":\"").append(hist.getHistory_end_time()).append("\", ");
				
				String input_code = escape(String.valueOf(hist.getHistory_input()));
				
				resp.append("\"input\":\"").append(input_code).append("\", ");
				
				String output_code = escape(String.valueOf(hist.getHistory_output()));
				
				resp.append("\"output\":\"").append(output_code).append("\", ");
				
				resp.append("\"category\":\"").append(escape(String.valueOf(thep.getDescription()))).append("\", ");
				
				resp.append("\"host\":\"").append(escape(String.valueOf(hist.getHost_id()))).append("\", ");
				
				resp.append("\"status\":\"").append(String.valueOf(hist.getIndicator())).append("\" }");
				
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
				
				Object[] row = active_processes.get(num);
				
				resp.append("{ \"id\": \"").append(row[0]).append("\", ");
				
				resp.append("\"begin_time\": \"").append(row[1]);
				
				resp.append("\", \"end_time\": \"").append(row[2]);
				
				resp.append("\", \"output\": \"").append(escape(String.valueOf(row[3])));
				
				resp.append("\", \"status\": \"").append(escape(String.valueOf(row[4])));
				
				resp.append("\", \"notes\": \"").append(escape(String.valueOf(row[8])));
				
				resp.append("\", \"host\": \"").append(escape(String.valueOf(row[5])));
				
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
	public String all_history(String pid) {
		
		return history_tool.process_all_history(pid);
		
	}
	
	
	
	
}
