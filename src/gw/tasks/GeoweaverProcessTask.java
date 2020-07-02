package gw.tasks;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import gw.database.DataBaseOperation;
import gw.tools.FileTool;
import gw.tools.ProcessTool;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;

/**
 * 
 * Task for builtin processes
 * 
 * @author JensenSun
 *
 */
public class GeoweaverProcessTask  extends Task {
	
	String 	   name;
			
	String	   pid;
		   
	String	   host;
		   
	String	   pswd;
		   
	String	   token;
	
	boolean	   isjoin;
		   
   	String	   history_input;
    
    String	   history_output;
    
    String	   history_begin_time;
    
    String	   history_end_time;
    
    String 	   history_id;
    
    Logger logger = Logger.getLogger(this.getClass());
	
	WebSocketSession monitor = null;
	
	public GeoweaverProcessTask(String name) {
		
		this.name = name;
		
	}
	
	public void initialize(String pid, String host, String pswd, String token, boolean isjoin) {
		
		this.pid = pid;
		
		this.host = host;
		
		this.pswd = pswd;
		
		this.token = token;
		
		this.history_id = new RandomString(11).nextString();
		
		this.isjoin = isjoin;
		
	}
	
	public String getHistory_id() {
		return history_id;
	}
	
	/**
	 * Start the monitoring of the task
	 * @param socketsession
	 */
	public void startMonitor(WebSocketSession socketsession) {
		
		monitor = socketsession;
		
	}
	
	/**
	 * Stop the monitoring of the task
	 */
	public void stopMonitor() {
		
		try {
			
			logger.info("close the websocket session from server side");
			
			if(!BaseTool.isNull(monitor))
				monitor.close();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
		
	}

	@Override
	public void initialize() {
		
		
	}

	@Override
	public void execute() {

		logger.info(" + + + start Geoweaver Process " + pid );
		
		try {

			Thread.sleep(1); //sleep 1s to wait for the client to catch up
			
			//get the nodes and edges of the workflows
			
			this.history_begin_time = BaseTool.getCurrentMySQLDatetime();
			
			this.history_output = "";
			
			String code = ProcessTool.getCodeById(pid);
			
			this.history_input = code;
			
			JSONObject obj = (JSONObject)new JSONParser().parse(code);
			
			String operation = (String)obj.get("operation");
			
			JSONArray params = (JSONArray)obj.get("params");
			
			if(operation.equals("ShowResultMap") || operation.equals("DownloadData") ) {
				
				String filepath = (String)((JSONObject)params.get(0)).get("value");
				
				logger.info("get result file path : " + filepath);
				
//				String filename = new RandomString(8).nextString();
				
				String filename = new File(filepath).getName();
				
//				String dest = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/" + filename;
				
				File folder = new File(BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path);
				
				if(!folder.exists()) {
					folder.mkdir();
				}
				
				String fileloc = BaseTool.getCyberConnectorRootPath() + SysDir.upload_file_path + "/" + filename;
				
				FileTool.scp_download(host, pswd, filepath, fileloc);
				
				logger.info("result info: " + fileloc);
				
				String ret = "{\"builtin\": true, \"history_id\": \"" + this.history_id + 
						"\", \"operation\":\""+operation+"\", \"filename\": \"" + filename + "\"}";
				
				if(monitor!=null) 
					monitor.sendMessage(new TextMessage(ret));
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
		
		this.history_end_time = BaseTool.getCurrentMySQLDatetime();
    	
    	StringBuffer sql = new StringBuffer("insert into history (id, process, begin_time, end_time, input, output, host) values ('");
    	
    	sql.append(this.history_id).append("','");
    	
    	sql.append(this.pid).append("','");
    	
    	sql.append(this.history_begin_time).append("','");
    	
    	sql.append(this.history_end_time).append("',?, ?,'");
    	
    	sql.append(this.host).append("' )");
    	
    	DataBaseOperation.preexecute(sql.toString(), new String[] {this.history_input, this.history_output});
		
	}

	@Override
	public void responseCallback() {

		logger.info("Process "+ name +" is finished!");
		
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
