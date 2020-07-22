package gw.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import gw.log.ExecutionStatus;
import gw.log.History;
import gw.process.GWProcess;
import gw.ssh.SSHCmdSessionOutput;
import gw.tools.HistoryTool;
import gw.tools.ProcessTool;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;
import gw.ws.server.CommandServlet;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

/**
 * 
 * This is for Linux/Mac
 * 
 * @author JensenSun
 *
 */
public class LocalSessionNixImpl implements LocalSession {

	Logger log  = Logger.getLogger(this.getClass());
	
	private boolean			 isTerminal;
	
	private BufferedReader   input;
	    
    private OutputStream     output;

    private LocalSessionOutput sender;
    
    private Thread           thread;
    
    private String           token;
    
    private History          history = new History();
    
    private HistoryTool      history_tool = new HistoryTool();
    
    private String           tempfile;
    
    /**
	 * Initialize history object when process execution starts
	 * @param script
	 * @param processid
	 * @param isjoin
	 * @param token
	 */
	public void initHistory(String script, String processid, boolean isjoin, String token) {
		
		this.token = token;
		
		this.isTerminal = isjoin;
		
		history = history_tool.initProcessHistory(history, processid, script);
		
	}
	
	/**
	 * If the process ends with error
	 * @param token
	 * @param message
	 */
	public void endWithError(String token, String message) {
		
		try {
			
			Session wsout = CommandServlet.findSessionById(token);
			
			if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
				
				log.info("The failed message has been sent to client");
				
				wsout.getBasicRemote().sendText(message);
				
				wsout.getBasicRemote().sendText("The process " + this.history.getHistory_id() + " is stopped.");
				
			}
			
		} catch (IOException e1) {
			
			e1.printStackTrace();
			
		}
		
		this.stop();
		
		this.history.setHistory_end_time(BaseTool.getCurrentMySQLDatetime());
		
		this.history.setHistory_output(message);
		
		this.history.setIndicator(ExecutionStatus.FAILED);
		
		this.history_tool.saveHistory(this.history);
		
	}
    
	@Override
	public void runBash(String script, String processid, boolean isjoin, String token) {
		
//		this.history = history_tool.initProcessHistory(history, processid, script);
		
		this.initHistory(script, processid, isjoin, token);
    	
    	try {
    		
    		log.info("starting command");
    		
    		tempfile = SysDir.workspace + "/gw-" + token + "-" + history.getHistory_id() + ".sh";

    		script += "\n echo \"==== Geoweaver Bash Output Finished ====\"";
    		
    		BaseTool.writeString2File(script, tempfile);
    		
    		Runtime.getRuntime().exec(new String[] {"chmod", "+x", tempfile});
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(SysDir.workspace));
    		
    		builder.command(tempfile);
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
    		input = new BufferedReader(new InputStreamReader(stdout));
            
    		sender = new LocalSessionOutput(input, token);
    		
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
	public void runJupyter(String notebookjson, String processid, boolean isjoin, String bin, String env, String basedir,
			String token) {
		
		this.initHistory(notebookjson, processid, isjoin, token);
		
    	try {
    		
    		log.info("starting command");
    		
    		tempfile = SysDir.workspace + "/gw-" + token + "-" + history.getHistory_id() + ".ipynb";

    		BaseTool.writeString2File(notebookjson, tempfile);
    		
    		// Get a list of all environment variables
            final Map<String, String> envMap = new HashMap<String, String>(System.getenv());
            
    		if(BaseTool.isNull(bin)||"default".equals(bin)) {

//    			cmdline += "python python-" + history_id + ".py;";
    			
//    			cmdline += "python " + filename + "; ";
    			
    		}else {
    			
//    			cmdline += "conda init; ";
    			
//    			cmdline += "source activate " + env + "; "; //for demo only
    			
    			Runtime.getRuntime().exec(new String[] {"source", "activate", env});
    			
//    			cmdline += bin + " " + filename + "; ";
    			
    		}
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(SysDir.workspace));
    		
    		builder.command(new String[] {"jupyter", "nbconvert", "--to", "notebook", "--execute", tempfile} );
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout));
            
//            sender = new SSHSessionOutput(input, token);
            sender = new LocalSessionOutput(input, token);
            
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
	public void runPython(String python, String processid, boolean isjoin, String bin, String pyenv, String basedir,
			String token) {
		
		this.initHistory(python, processid, isjoin, token);
    	
    	try {
    		
    		log.info("save to local file: " + python);
    		
    		GWProcess pro = ProcessTool.getProcessById(processid);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
    		builder.directory(new File(SysDir.workspace + "/" + token));
    		
    		String pythonfilename = pro.getName();
    		
    		if(!pythonfilename.endsWith(".py")) pythonfilename += ".py";
    		
    		builder.command(new String[] {"python", pythonfilename} );
    		
    		builder.redirectErrorStream(true);
    		
    		Process process = builder.start();
    		
    		InputStream stdout = process.getInputStream ();
    		
            log.info("Local session established");
            
            input = new BufferedReader(new InputStreamReader(stdout));
    		
            sender = new LocalSessionOutput(input, token);
            
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
	public void runMultipleBashes(String[] script, String processid) {
		
		
		
	}

	@Override
	public void saveHistory(String logs, String status) {
		
		history.setHistory_output(logs);
		
		history.setIndicator(status);
		
		history_tool.saveHistory(history);
		
		ProcessTool.updateJupyter(history, this.token);
		
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
		
		return false;
	}

	@Override
	public boolean clean() {
		
		File temp = new File(tempfile);
		
		return temp.delete();
		
	}
	

}
