package com.gw.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.websocket.Session;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import  com.gw.server.CommandServlet;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * For Windows
 * 
 * @author JensenSun
 *
 */
@Service
public class LocalSessionWinImpl implements LocalSession {

	Logger log  = Logger.getLogger(this.getClass());
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	BaseTool bt;
	
	@Autowired
    private HistoryTool      history_tool;
	
	private boolean			 isTerminal;
	
	private BufferedReader   input;
	    
    private OutputStream     output;

    @Autowired
    private LocalSessionOutput sender;
    
    private Thread           thread;
    
    private String           token;
    
    private String           tempfile;
    
    private History          history;
    
    private Process          process;
    
    @Value("${geoweaver.workspace}")
    private String           workspace_folder_path;
    
    public LocalSessionWinImpl() {
    	
    	//this is for spring
    	
    }
    
	
    @Override
	public String getToken() {
		return this.token;
	}

	@Override
	public boolean isTerminal() {
		return this.isTerminal;
	}

	@Override
	public BufferedReader getLocalInput() {
		return input;
	}

	@Override
	public History getHistory() {
		
		return this.history;
	}

	@Override
	public void setHistory(History history) {
		
		this.history = history;
		
	}
	
	/**
	 * If the process ends with error
	 * @param token
	 * @param message
	 */
	public void endWithError(String token, String message) {
		
		try {
			
			Session wsout = CommandServlet.findSessionById(token);
			
			// synchronized(wsout) {

				if(!bt.isNull(wsout) && wsout.isOpen()) {
					
					log.info("The failed message has been sent to client");
					
					wsout.getBasicRemote().sendText(message);
					
					wsout.getBasicRemote().sendText("The process " + this.history.getHistory_id() + " is stopped.");
					
				}
				
			// }
			
		} catch (IOException e1) {
			
			e1.printStackTrace();
			
		}
		
		this.stop();
		
		this.history.setHistory_end_time(bt.getCurrentSQLDate());
		
		this.history.setHistory_output(message);
		
		this.history.setIndicator(ExecutionStatus.FAILED);
		
		this.history_tool.saveHistory(this.history);
		
	}
	
	/**
	 * Initialize history object when process execution starts
	 * @param script
	 * @param processid
	 * @param isjoin
	 * @param token
	 */
	public void initHistory(String history_id, String script, String processid, boolean isjoin, String token) {
		
		this.token = token;
		
		this.isTerminal = isjoin;
		
		history = history_tool.initProcessHistory(history_id, processid, script);
		
	}
	
	@Override
	public void saveHistory(String logs, String status) {
		
//		log.info("Save History in LocalSessionWIn is called ");
		
		this.history.setHistory_output(logs);
		
		this.history.setIndicator(status);
		
		history_tool.saveHistory(history);
		
		pt.updateJupyter(history, this.token);
		
	}
	
	@Override
	public void runBash(String history_id, String script, String processid, boolean isjoin, String token) {
		
		this.initHistory(history_id, script, processid, isjoin, token);
    	
    	try {
    		
    		log.info("starting command");
    		
    		String rand = new RandomString(3).nextString();
    		
    		tempfile = workspace_folder_path + "/gw-" + token + "-" + rand + ".sh";

    		script += "\n echo \"==== Geoweaver Bash Output Finished ====\"";
    		
    		bt.writeString2File(script, tempfile);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
//    		// -- Windows --
//
//    		// Run a command
    		builder.command("bash.exe", "-c", tempfile); //bash.exe of cygwin must be in the $PATH
//
//    		// Run a bat file
//    		processBuilder.command("C:\\Users\\mkyong\\hello.bat");
//    		builder.command(tempfile);
    		
    		builder.redirectErrorStream(true);
    		
    		process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
    		input = new BufferedReader(new InputStreamReader(stdout));
            
    		sender.init(input, token, history_id);
    		
    		thread = new Thread(sender);
            
            thread.setName("Local Command output thread");
            
            log.info("starting sending thread from local command");
            
            thread.start();
            
            log.info("returning to the client..");
    		
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}
    	
		
	}
	

	@Override
	public void runJupyter(String history_id, String script, String processid, boolean isjoin, String bin, String env, String basedir,
			String token) {

		this.initHistory(history_id, script, processid, isjoin, token);
		
    	try {
    		
    		GWProcess pro = pt.getProcessById(processid);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(workspace_folder_path + "/" + token));
    		
    		String pythonfilename = pro.getName();
    		
    		log.info("Start to execute jupyter notebook: " + pythonfilename);
    		
    		if(!pythonfilename.endsWith(".ipynb")) pythonfilename += ".ipynb";
    		
    		builder.command(new String[] {"jupyter", "nbconvert", "--to", "notebook", "--execute", pythonfilename} );
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout));
            
            sender.init(input, token, history_id);
            
            thread = new Thread(sender);
            
            thread.setName("SSH Command output thread");
            
            log.info("starting sending thread");
            
            thread.start();
            
            log.info("returning to the client..");
            
            if(isjoin) thread.join(7*24*60*60*1000); //longest waiting time - a week
	        
            log.info("Local Session Windows Implementation is done.");
            
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}
    	
	}

	@Override
	public void runPython(String history_id, String python, String processid, boolean isjoin, String bin, 
			String pyenv, String basedir, String token) {
		
		this.initHistory(history_id, python, processid, isjoin, token);
		
    	try {
    		
    		// log.info("save to local file: " + python);

    		GWProcess pro = pt.getProcessById(processid);
    		
    		ProcessBuilder builder = new ProcessBuilder();

			String realpath = bt.normalizedPath(workspace_folder_path + "/" + token);
    		
			log.info("Setting the working directory to " + realpath);

    		builder.directory(new File(realpath));
			
    		String pythonfilename = pro.getName();
    		
    		if(!pythonfilename.endsWith(".py")) pythonfilename += ".py";
    		
    		builder.command(new String[] {"python", pythonfilename} );
    		
    		builder.redirectErrorStream(true);
    		
    		process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout));
            
            sender.init(input, token, history_id);
            
            //moved here on 10/29/2018
            //all SSH sessions must have a output thread
            
            thread = new Thread(sender);
            
            thread.setName("SSH Command output thread");
            
            log.info("starting sending thread");
            
            thread.start();
            
            log.info("returning to the client..");
            
            if(isjoin) thread.join(7*24*60*60*1000); //longest waiting time - a week
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}
		
	}

	@Override
	public void runMultipleBashes(String history_id, String[] script, String processid) {
		
		throw new RuntimeException("Not Supported Yet");
		
	}

	

	@Override
	public boolean stop() {

		// log.debug("Is process alive? " + process.isAlive());

		// log.debug("Is thread alive? " + thread.isAlive()); //this thread will stop by itself after the task is finished.

		// if(thread.isAlive()) thread.interrupt();

		log.debug("for localhost session, there is nothing to manually stop. Just wait for the process to finish. That is all.");
		
		// if(!bt.isNull(process)) {
			
		// 	process.destroy();
			
		// }
		
		// if(!bt.isNull(thread)) {
			
		// 	thread.interrupt();
			
		// }
		
		// if(!bt.isNull(input)) {
			
		// 	try {
		// 		input.close();
		// 	} catch (IOException e) {
		// 		e.printStackTrace();
		// 	}
			
		// }
		
		return true;
		
	}

	@Override
	public boolean clean() {
		
		File temp = new File(tempfile);
		
		return temp.delete();
		
	}

	

	

}
