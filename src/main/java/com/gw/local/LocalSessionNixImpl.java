package com.gw.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import com.fasterxml.jackson.databind.JsonSerializable.Base;
import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;
import com.gw.tools.EnvironmentTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.OSValidator;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * This class represents the implementation of a local session for Linux and Mac.
 * It provides methods for running commands and scripts on the local system.
 * 
 * @author JensenSun
 */
@Service
@Scope("prototype")
public class LocalSessionNixImpl implements LocalSession {

	Logger log  = Logger.getLogger(this.getClass());
	
	@Autowired
	ProcessTool pt;
	
	@Autowired
	BaseTool bt;

	@Autowired
	HostTool ht;

	@Autowired
	EnvironmentTool et;
	
	@Autowired
	HistoryTool history_tool;
	
	private boolean			 isTerminal;

	private boolean          isClose;
	
	private BufferedReader   input;
	    
//    private OutputStream     output;

	@Autowired
    private LocalSessionOutput sender;
    
    private Thread           thread;
    
	/**
	 * Http Session Id
	 */
    private String           token;
    
    private History          history;
//    
//    private HistoryTool      history_tool = new HistoryTool();
    
    private String           tempfile;
    
    @Value("${geoweaver.workspace}")
    private String           workspace_folder_path;

	private Process          process;
    
	/**
     * Default constructor for the LocalSessionNixImpl class.
     */
    public LocalSessionNixImpl() {
    	
    	//this is for spring
		log.info("localsessionniximpl object is created for " + token); 
    	
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

		this.isClose = false;
		
		history = history_tool.initProcessHistory(history_id, processid, script);

		// this.sender = createLocalSessionOutput(); 
		
	}
	
	/**
	 * If the process ends with error
	 * @param token
	 * @param message
	 */
	public void endWithError(String token, String message) {
		
		this.stop();
		
		this.history.setHistory_end_time(BaseTool.getCurrentSQLDate());
		
		this.history.setHistory_output(message);
		
		this.history.setIndicator(ExecutionStatus.FAILED);
		
		this.history_tool.saveHistory(this.history);

		this.isClose = true;
		
		if(!BaseTool.isNull(message))
		CommandServlet.sendMessageToSocket(token, this.history.getHistory_id()+BaseTool.log_separator+message);
	
		CommandServlet.sendMessageToSocket(token, this.history.getHistory_id()+BaseTool.log_separator+
			"The process " + this.history.getHistory_id() + " is stopped.");
	
	}
    
	/**
	 * Runs a Bash script as a new process and handles its execution.
	 * 
	 * @param history_id The unique identifier for the history of the execution.
	 * @param script     The Bash script to execute.
	 * @param processid  The unique identifier for the process.
	 * @param isjoin     A boolean flag indicating whether to wait for the script execution to complete (join).
	 * @param token      The HTTP session token associated with the execution.
	 */
	@Override
	public void runBash(String history_id, String script, String processid, boolean isjoin, String token) {
		// Initialize the history object for this execution.
		this.initHistory(history_id, script, processid, isjoin, token);
		
		try {
			// Log that the command execution is starting.
			log.info("Starting command execution");
			
			// Create a temporary file to store the Bash script.
			tempfile = bt.normalizedPath(workspace_folder_path) + "/" + history_id + "/gw-" + token + "-" + history.getHistory_id() + ".sh";
			
			// Write the Bash script content to the temporary file.
			bt.writeString2File(script, tempfile);
			
			// Change the permissions of the temporary script file to make it executable.
			Runtime.getRuntime().exec(new String[] {"chmod", "+x", tempfile}).waitFor();
			
			// Wait for a short duration (1 millisecond) to ensure that the file is properly created.
			bt.sleep(1);
			
			// Create a ProcessBuilder for executing the Bash script.
			ProcessBuilder builder = new ProcessBuilder();
			
			// Set the working directory for the process.
			builder.directory(new File(bt.normalizedPath(workspace_folder_path) + "/" + history_id));
			
			// Specify the command to execute (the path to the temporary script file).
			builder.command(tempfile);
			
			// Redirect the error stream to the standard output stream.
			builder.redirectErrorStream(true);
			
			// Start the process for executing the Bash script.
			process = builder.start();
			
			// Get the standard output stream of the process.
			InputStream stdout = process.getInputStream();
			
			// Initialize a buffered reader to read the standard output.
			input = new BufferedReader(new InputStreamReader(stdout), BaseTool.BUFFER_SIZE);
			
			// Initialize the sender with the input stream, HTTP session token, history ID, execution type (shell), and a null value for the script.
			sender.init(input, token, history_id, "shell", null);
			
			// Create a new thread for sending the script's output.
			thread = new Thread(sender);
			
			// Set the name of the thread for identifying it.
			thread.setName("Local Command output thread");
			
			// Log that the sending thread for local command output is starting.
			log.info("Starting sending thread from local command");
			
			// Start the sending thread.
			thread.start();
			
			// Set the process associated with the sender.
			sender.setProcess(process);
			
			// If isjoin is true, wait for the process to complete.
			if (isjoin) {
				process.waitFor();
			}
			
			// Log that the command execution has completed and is returning to the client.
			log.info("Command execution completed, returning to the client");
		} catch (Exception e) {
			// Handle any exceptions that occur during the execution.
			e.printStackTrace();
			
			// End the execution with an error message.
			this.endWithError(token, e.getLocalizedMessage());
		} finally {
			// Mark the execution as closed.
			this.isClose = true;
		}
	}


	@Override
	public void runJupyter(String history_id, String notebookjson, String processid, 
			boolean isjoin, String bin, String env, String basedir,
			String token) {
		
		this.initHistory(history_id, notebookjson, processid, isjoin, token);
		
    	try {
    		
    		log.info("starting command");
    		
    		tempfile = bt.normalizedPath(workspace_folder_path) + "/" + history_id + "/gw-" + token + "-" + history.getHistory_id() + ".ipynb";

    		bt.writeString2File(notebookjson, tempfile);
    		
    		// Get a list of all environment variables
            Map<String, String> envMap = new HashMap<String, String>(System.getenv());
            
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(bt.normalizedPath(workspace_folder_path)+ "/" + history_id));
    		
			if(BaseTool.isNull(bin)){
			
				builder.command(new String[] {"jupyter", "nbconvert", "--inplace", "--allow-errors", "--to", "notebook", "--execute", tempfile} );
			
			}else{

				builder.command(new String[] {bin, "-m", "jupyter", "nbconvert", "--inplace", "--allow-errors", "--to", "notebook", "--execute", tempfile} );
			
			}
    		
    		builder.redirectErrorStream(true);
    		
    		process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout), BaseTool.BUFFER_SIZE);
            
            sender.init(input, token, history_id, "juyter", tempfile);
            
            //moved here on 10/29/2018
            //all SSH sessions must have a output thread
            
            thread = new Thread(sender);
            
            thread.setName("SSH Command output thread");
            
            log.info("starting sending thread");
            
            thread.start();
            
            log.info("returning to the client..");
            
			sender.setProcess(process);

			if(isjoin){

				process.waitFor();

			}

            
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}finally{

			this.isClose = true;

		}
		
	}

	@Override
	public void runPython(String history_id, String python, String processid, boolean isjoin, String bin, String pyenv, String basedir, String token) {
		
		this.initHistory(history_id, python, processid, isjoin, token);
    	
    	try {
    		
    		//log.info("save to local file: " + python);
    		
    		GWProcess pro = pt.getProcessById(processid);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(bt.normalizedPath(workspace_folder_path) + "/" + history_id));
    		
    		String pythonfilename = pro.getName();
    		
    		if(!pythonfilename.endsWith(".py")) pythonfilename += ".py";

			if(BaseTool.isNull(bin)) bin = "python";
    		
    		builder.command(new String[] {bin, "-u", pythonfilename} );
    		
    		builder.redirectErrorStream(true);
    		
    		process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout), BaseTool.BUFFER_SIZE);
    		
            sender.init(input, token, history_id, "python", null);
            
            //moved here on 10/29/2018
            //all SSH sessions must have a output thread
            
            thread = new Thread(sender);
            
            thread.setName("SSH Command output thread");
            
            log.info("starting sending thread");
            
            thread.start();
            
			sender.setProcess(process);

            log.info("returning to the client..");
            
			if(isjoin){

				process.waitFor();

			}

			
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}finally{

			this.isClose = true;

		}
		
	}

	@Override
	public void runMultipleBashes(String history_id, String[] script, String processid) {
		
		
		
	}

	@Override
	public void saveHistory(String logs, String status) {
		
		history.setHistory_output(logs);
		
		history.setIndicator(status);
		
		history_tool.saveHistory(history);
		
		pt.updateJupyter(history, this.token);
		
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
		
		return this.input;
		
	}

	@Override
	public History getHistory() {
		
		return this.history;
		
	}

	@Override
	public void setHistory(History history) {
		
		this.history = history;
		
	}

	@Override
	public boolean stop() {
		
		try{

			if(!BaseTool.isNull(process)) process.destroy();

			return true;

		}catch(Exception e){

			return false;

		}
	}

	@Override
	public boolean clean() {
		
		File temp = new File(tempfile);
		
		return temp.delete();
		
	}

	void readWhere(String hostid, String password) throws IOException, InterruptedException{

		try{

			//read existing environments
			List<Environment> old_envlist = et.getEnvironmentsByHostId(hostid);
			
			if(!OSValidator.isMac()){

				List<String> cmds = new ArrayList();
				cmds.add("whereis");
				cmds.add("python");

				List<String> stdout = bt.executeLocal(cmds);

				//get all the python path
				for(String line: stdout){

					if(!BaseTool.isNull(line)){

						if(line.startsWith("python")){

							String pythonarraystr = line.substring(8);

							String[] pythonarray = pythonarraystr.split("\\s+");

							for(String pypath : pythonarray){

								if(!BaseTool.isNull(pypath)){
				
									pypath = pypath.trim();
				
									et.addNewEnvironment(pypath, old_envlist, hostid, pypath);
				
								}
				
							}

						}

					}
					
				}

			}else{

				List<String> cmds = new ArrayList();
				cmds.add("sh");
				cmds.add("-c");
				cmds.add("ls /usr/bin/python*");

				List<String> stdout = bt.executeLocal(cmds);

				//get all the python path
				for(String line: stdout){

					if(!BaseTool.isNull(line)){

						String pythonarraystr = line;

						String[] pythonarray = pythonarraystr.split("\\s+");

						for(String pypath : pythonarray){

							if(!BaseTool.isNull(pypath)){
			
								pypath = pypath.trim();
			
								et.addNewEnvironment(pypath, old_envlist, hostid, pypath);
			
							}
			
						}

					}
					
				}

			}

			

		}catch(Exception e){

			e.printStackTrace();

		}

	}

	void readConda(String hostid, String password) throws IOException, InterruptedException{

		try{

			//read existing environments
			List<Environment> old_envlist = et.getEnvironmentsByHostId(hostid);
				
			List<String> cmds = new ArrayList();
			cmds.add("conda");
			cmds.add("env");
			cmds.add("list");

			List<String> stdout = bt.executeLocal(cmds);

			if(stdout.size()>0 && stdout.get(0).startsWith("# conda")){

				//get all the python path
				for(String line: stdout){

					if(!BaseTool.isNull(line) && !line.startsWith("#")){

						String[] vals = line.split("\\s+");

						if(vals.length<2) continue;

						String bin = vals[vals.length-1]+"/bin/python";

						String name = BaseTool.isNull(vals[0])?bin:vals[0];

						Environment theenv = et.getEnvironmentByBin(bin, old_envlist);

						if(BaseTool.isNull(theenv)){

							et.addNewEnvironment(bin, old_envlist, hostid, name);

						}else{

							//if want to update the settings, do it here

						}

					}
					
				}

			}else{

				log.debug("Conda environments are not found.");

			}
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}

	@Override
	public String readPythonEnvironment(String hostid, String password) {

		String resp = null;

		try {

			this.readWhere(hostid, password);

			this.readConda(hostid, password);

			resp = et.getEnvironments(hostid);

		} catch (Exception e) {

			e.printStackTrace();

		}

		return resp;

	}

	@Override
	public boolean isClose() {
		return false;
	}
	

}
