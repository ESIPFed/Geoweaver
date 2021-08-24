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

	String     bin;

	String     pyenv;

	String     basedir;
    
    @Value("${geoweaver.upload_file_path}")
    String     upload_file_path;
    
    Logger logger = Logger.getLogger(this.getClass());
	
    javax.websocket.Session monitor = null;
    
    public GeoweaverProcessTask() {
    	
    	//for spring
    	
    }
	
	public void initialize(String pid, String host, String pswd, String token, boolean isjoin, String bin, String pyenv, String basedir) {
		
		this.pid = pid;
		
		this.host = host;
		
		this.pswd = pswd;
		
		this.token = token;
		
		this.history_id = new RandomString(11).nextString();
		
		this.isjoin = isjoin;

		this.bin = bin;

		this.pyenv = pyenv;

		this.basedir = basedir;
		
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

			pt.execute(pid, host, pswd, token, isjoin, bin, pyenv, basedir);
			
			if(monitor!=null) {
                
                monitor.getAsyncRemote().sendText("");
//					monitor.sendMessage(new TextMessage(ret));
                
            }
            else
                logger.warn("Monitor websocket session should not be null!");
            
            
			
			
			
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

		logger.debug("Process "+ this.history_id +" is finished!");
		
	}

	@Override
	public void failureCallback(Exception e) {
		
		logger.error("Process execution is failed " + e.getLocalizedMessage());
		
	}

	@Override
	public String getName() {
		return this.getName();
	}


}
