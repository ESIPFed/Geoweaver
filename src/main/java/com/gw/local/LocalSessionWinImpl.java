package com.gw.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.websocket.Session;

import com.gw.jpa.Environment;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import  com.gw.server.CommandServlet;
import com.gw.tools.EnvironmentTool;
import com.gw.tools.HistoryTool;
import com.gw.tools.HostTool;
import com.gw.tools.ProcessTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

/**
 * 
 * For Windows
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
public class LocalSessionWinImpl implements LocalSession {

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
    private HistoryTool      history_tool;
	
	private boolean			 isTerminal;

	private boolean          isClose;
	
	private BufferedReader   input;
	
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
		
		this.stop();
		
		this.history.setHistory_end_time(BaseTool.getCurrentSQLDate());
		
		this.history.setHistory_output(message);
		
		this.history.setIndicator(ExecutionStatus.FAILED);
		
		this.history_tool.saveHistory(this.history);

		this.isClose = true;

		CommandServlet.sendMessageToSocket(token, message);

		CommandServlet.sendMessageToSocket(token, "======= Process " + this.history.getHistory_id() + " ended.");
		
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
    		
    		tempfile = bt.normalizedPath(workspace_folder_path) + "/" + token + "/gw-" + token + "-" + rand + ".sh";

    		// script += "\n echo \"==== Geoweaver Bash Output Finished ====\"";
    		
    		bt.writeString2File(script, tempfile);
    		
    		ProcessBuilder builder = new ProcessBuilder();

			builder.directory(new File(bt.normalizedPath(workspace_folder_path) + "/" + token));
    		
//    		// -- Windows --
//    		// Run a command
    		builder.command("bash.exe", "-c", tempfile); //bash.exe of cygwin must be in the $PATH

    		builder.redirectErrorStream(true);
    		
    		process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
    		input = new BufferedReader(new InputStreamReader(stdout), BaseTool.BUFFER_SIZE);
            
    		sender.init(input, token, history_id, "shell", null);
    		
    		thread = new Thread(sender);
            
            thread.setName("Local Command output thread");
            
            log.info("starting sending thread from local command");
            
            thread.start();
			
			sender.setProcess(process);

			if(isjoin){

				process.waitFor();

			}

            
            log.info("returning to the client..");
    		
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}finally{

			this.isClose = true;

		}
    	
		
	}
	

	@Override
	public void runJupyter(String history_id, String script, String processid, boolean isjoin, String bin, String env, String basedir,
			String token) {

		this.initHistory(history_id, script, processid, isjoin, token);
		
    	try {
    		
    		GWProcess pro = pt.getProcessById(processid);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(bt.normalizedPath(workspace_folder_path) + "/" + history_id)); // this folder is only used to find data files, not the execution command
    		
    		String pythonfilename = pro.getName();
    		
    		log.info("Start to execute jupyter notebook: " + pythonfilename);
    		
    		if(!pythonfilename.endsWith(".ipynb")) pythonfilename += ".ipynb";
			
			pythonfilename = bt.normalizedPath(workspace_folder_path) + "/" + history_id + "/" + pythonfilename;
    		
			if(BaseTool.isNull(bin)){
				builder.command(new String[] {"jupyter", "nbconvert", "--inplace", "--to", "notebook", "--allow-errors", "--execute", pythonfilename} );
			}else{
				builder.command(new String[] {bin, "-m", "jupyter", "nbconvert", "--inplace", "--to", "notebook", "--allow-errors", "--execute", pythonfilename} );
			}
    		
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout), BaseTool.BUFFER_SIZE);
            
            sender.init(input, token, history_id, pro.getLang(), pythonfilename);
            
            thread = new Thread(sender);
            
            thread.setName("SSH Command output thread");
            
            log.info("starting sending thread");
            
            thread.start();

			sender.setProcess(process);
            
            log.info("returning to the client..");
            
			if(isjoin){

				process.waitFor();

			}
	        
            log.info("Local Session Windows Implementation is done.");
            
		} catch (Exception e) {
			
			e.printStackTrace();
			
			this.endWithError(token, e.getLocalizedMessage());
			
		}finally{

			this.isClose = true;

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
			
			Map<String, String> env = builder.environment();

			env.put("Path", env.get("Path")+";");

			String realpath = bt.normalizedPath(workspace_folder_path + "/" + history_id);
    		
			log.info("Setting the working directory to " + realpath);

    		builder.directory(new File(realpath));
			
    		String pythonfilename = pro.getName();
    		
    		if(!pythonfilename.endsWith(".py")) pythonfilename += ".py";

			if(BaseTool.isNull(bin)) bin = "python";
    		
    		builder.command(new String[] {bin, "-u", pythonfilename} );
    		
			// log.info(builder.environment());
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
	public void runMultipleBashes(String history_id, String[] script, String processid) {
		
		throw new RuntimeException("Not Supported Yet");
		
	}
	
	@Override
	public boolean stop() {

		log.debug("for localhost session, there is nothing to manually stop. Just wait for the process to finish. That is all.");
		
		try{

			if(!BaseTool.isNull(process)) {
			
				process.destroy();
				
			}

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

	void readWhere(String hostid, String password){
		//read existing environments
		List<Environment> old_envlist = et.getEnvironmentsByHostId(hostid);
			
		List<String> cmds = new ArrayList();
		cmds.add("where");
		cmds.add("python.exe");

		List<String> stdout = bt.executeLocal(cmds);

		//get all the python path
		for(String line: stdout){
			
			Environment theenv = et.getEnvironmentByBin(line, old_envlist);

			if(BaseTool.isNull(theenv)){

				Environment env = new Environment();
				env.setId(new RandomString(6).nextString());
				env.setBin(line);
				env.setName(line);
				env.setHostobj(ht.getHostById(hostid));
				// env.setBasedir(line); //the execution place which is unknown at this point
				if(line.contains("conda"))
					env.setPyenv("anaconda");
				else
					env.setPyenv("pip");
				env.setSettings(""); //set the list of dependencies like requirements.json or .yaml
				env.setType("python"); //could be python or shell. R is not supported yet. 
				env.setBasedir("~");
				ht.saveEnvironment(env);

			}else{

				//if want to update the settings, do it here

			}
			
		}
	}

	void readConda(String hostid, String password){

		//read existing environments
		List<Environment> old_envlist = et.getEnvironmentsByHostId(hostid);
			
		List<String> cmds = new ArrayList();
		cmds.add("conda");
		cmds.add("env");
		cmds.add("list");

		List<String> stdout = bt.executeLocal(cmds);
		if(stdout.get(0).startsWith("# conda")){

			//get all the python path
			for(String line: stdout){

				if(!BaseTool.isNull(line) && !line.startsWith("#")){

					String[] vals = line.split("\\s+");

					if(vals.length<2) continue;

					String bin = vals[vals.length-1]+"\\python.exe";

					String name = BaseTool.isNull(vals[0])?bin:vals[0];

					Environment theenv = et.getEnvironmentByBin(bin, old_envlist);

					if(BaseTool.isNull(theenv)){

						Environment env = new Environment();
						env.setId(new RandomString(6).nextString());
						env.setBin(bin);
						env.setName(name);
						// env.setHost(hostid);
						env.setHostobj(ht.getHostById(hostid));
						// env.setBasedir(line); //the execution place which is unknown at this point
						env.setPyenv("anaconda");
						env.setSettings(""); //set the list of dependencies like requirements.json or .yaml
						env.setType("python"); //could be python or shell. R is not supported yet. 
						env.setBasedir("~");
						ht.saveEnvironment(env);

					}else{

						//if want to update the settings, do it here

					}

				}
				
			}

		} else{
			log.debug("Conda environments are not found.");
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