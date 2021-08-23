package com.gw.tasks;

import java.io.File;
import java.util.Date;

import javax.websocket.Session;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.gw.jpa.History;
import com.gw.tools.FileTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import  com.gw.server.CommandServlet;

/**
 * 
 * Task for builtin processes
 * 
 * @author JensenSun
 *
 */
@Service
public class GeoweaverProcessTask  extends Task {
	
	@Autowired
	HostTool ht;
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
	FileTool ft;
	
	@Autowired
	HistoryTool hist;
	
	String 	   name;
			
	String	   pid;
		   
	String	   host;
		   
	String	   pswd;
		   
	String	   token;
	
	boolean	   isjoin;
		   
   	String	   history_input;
    
    String	   history_output;
    
    Date	   history_begin_time;
    
    Date	   history_end_time;
    
    String 	   history_id;
    
    @Value("${geoweaver.upload_file_path}")
    String     upload_file_path;
    
    Logger logger = Logger.getLogger(this.getClass());
	
    javax.websocket.Session monitor = null;
    
    public GeoweaverProcessTask() {
    	
    	//for spring
    	
    }
	
	public void initialize(String pid, String host, String pswd, String token, boolean isjoin, String name) {
		
		this.pid = pid;
		
		this.host = host;
		
		this.pswd = pswd;
		
		this.token = token;

		this.name = name;
		
		this.history_id = new RandomString(11).nextString();
		
		this.isjoin = isjoin;
		
		Session ws = CommandServlet.findSessionById(token);
		
		// if(bt==null) bt = new BaseTool();

		if(!bt.isNull(ws)) this.startMonitor(ws);
		
		
	}
	
	public String getHistory_id() {
		return history_id;
	}
	
	/**
	 * Start the monitoring of the task
	 * @param socketsession
	 */
	public void startMonitor(javax.websocket.Session socketsession) {
		
		monitor = socketsession;
		
	}
	
	/**
	 * Stop the monitoring of the task
	 */
	public void stopMonitor() {
		
		//no closing anymore, the websocket session between client and server should be always active
		
//		try {
//			
//			logger.info("close the websocket session from server side");
//			
////			if(!BaseTool.isNull(monitor))
////				monitor.close();
//			
//		} catch (IOException e) {
//			
//			e.printStackTrace();
//			
//		}
		
	}

	@Override
	public void initialize() {
		
		
	}

	@Override
	public void execute() {

		logger.debug(" + + + start Geoweaver Process " + pid );
		
		try {

			Thread.sleep(1); //sleep 1s to wait for the client to catch up
			
			//get the nodes and edges of the workflows
			
			this.history_begin_time = bt.getCurrentSQLDate();
			
			this.history_output = "";
			
			String code = pt.getCodeById(pid);
			
			this.history_input = code;
			
			JSONObject obj = (JSONObject)new JSONParser().parse(code);
			
			String operation = (String)obj.get("operation");
			
			JSONArray params = (JSONArray)obj.get("params");
			
			if(operation.equals("ShowResultMap") || operation.equals("DownloadData") ) {
				
				String filepath = (String)((JSONObject)params.get(0)).get("value");
				
				logger.debug("get result file path : " + filepath);
				
//				String filename = new RandomString(8).nextString();
				
				String filename = new File(filepath).getName();
				
//				String dest = BaseTool.getGeoweaverRootPath() + SysDir.upload_file_path + "/" + filename;
				
				File folder = new File(bt.getFileTransferFolder());
				
				if(!folder.exists()) {
					folder.mkdir();
				}
				
				String fileloc = bt.getFileTransferFolder() + "/" + filename;
				
				if(ht.islocal(host)) {
					
					ft.download_local(filepath, fileloc);
					
				}else {

					ft.scp_download(host, pswd, filepath, fileloc);
					
				}
				
				
				logger.debug("result info: " + fileloc);
				
				String ret = "{\"builtin\": true, \"history_id\": \"" + this.history_id + 
						"\", \"operation\":\""+operation+"\", \"filename\": \"" + filename + "\"}";
				
				if(monitor!=null) {
					
					monitor.getAsyncRemote().sendText(ret);
//					monitor.sendMessage(new TextMessage(ret));
					
				}
				else
					logger.warn("Monitor websocket session should not be null!");
				
				this.history_output = filename;
				
			}
			
		}catch(Exception e) {
			
			e.printStackTrace();
			
			this.history_output = e.getLocalizedMessage();
			
		}finally {
			
			if(!isjoin) this.stopMonitor(); //if run solo, close. if workflow, don't.
			
		}
		
		saveHistory();
		
	}
	
	public void saveHistory() {
		
		this.history_end_time = bt.getCurrentSQLDate();
		
		History history = hist.getHistoryById(this.history_id);
		
		history.setHistory_begin_time(this.history_begin_time);
		
		history.setHistory_end_time(this.history_end_time);
		
		history.setHistory_process(this.pid);
		
		history.setHistory_input(this.history_input);
		
		history.setHistory_output(this.history_output);
		
		history.setHost_id(this.host);
		
		hist.saveHistory(history);
    	
//    	StringBuffer sql = new StringBuffer("insert into history (id, process, begin_time, end_time, input, output, host) values ('");
//    	
//    	sql.append(this.history_id).append("','");
//    	
//    	sql.append(this.pid).append("','");
//    	
//    	sql.append(this.history_begin_time).append("','");
//    	
//    	sql.append(this.history_end_time).append("',?, ?,'");
//    	
//    	sql.append(this.host).append("' )");
//    	
//    	DataBaseOperation.preexecute(sql.toString(), new String[] {this.history_input, this.history_output});
		
	}

	@Override
	public void responseCallback() {

		logger.debug("Process "+ name +" is finished!");
		
	}

	@Override
	public void failureCallback(Exception e) {
		
		logger.error("Process execution is failed " + e.getLocalizedMessage());
		
	}

	@Override
	public String getName() {
		return name;
	}

}
