package gw.local;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import gw.log.ExecutionStatus;
import gw.log.History;
import gw.tools.HistoryTool;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;

/**
 * 
 * For Windows
 * 
 * @author JensenSun
 *
 */
public class LocalSessionWinImpl implements LocalSession {

	Logger log  = Logger.getLogger(this.getClass());
	
	private boolean			 isTerminal;
	
	private BufferedReader   input;
	    
    private OutputStream     output;

    private LocalSessionOutput sender;
    
    private Thread           thread;
    
    private String           token;
    
    private String           tempfile;
    
    private History          history = new History();
    
    private HistoryTool      history_tool = new HistoryTool();
    
    private Process          process;
	
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
	
	@Override
	public void runBash(String script, String processid, boolean isjoin, String token) {
		
//		this.history_id = token; //new RandomString(12).nextString();
		
		this.token = token;
		
		this.isTerminal = isjoin; //if is terminal, don't wait; if not, wait for it finishes. 
		
		log.info("processid: " + processid);
		
		this.history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		this.history.setHistory_begin_time(BaseTool.getCurrentMySQLDatetime());
		
		this.history.setHistory_input(script);
    	
    	try {
    		
    		log.info("starting command");
    		
    		String rand = new RandomString(3).nextString();
    		
    		tempfile = SysDir.workspace + "/gw-" + token + "-" + rand + ".sh";

    		script += "\n echo \"==== Geoweaver Bash Output Finished ====\"";
    		
    		BaseTool.writeString2File(script, tempfile);
    		
//    		String cmdline = "echo \"" 
//    				+ script.replaceAll("\r\n", "\n").replaceAll("\\$", "\\\\\\$").replaceAll("\"", "\\\\\"") 
//    				+ "\" > geoweaver-" + token + ".sh; ";
//    		
//    		cmdline += "chmod +x geoweaver-" + token + ".sh; ";
//    		
//    		cmdline += "./geoweaver-" + token + ".sh;";
//    		
//    		cmdline += "rm ./geoweaver-" + token + ".sh; "; //remove the script finally, leave no trace behind
			
//    		log.info(cmdline);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
//    		// -- Linux --
//
//    		// Run a shell command
//    		processBuilder.command("bash", "-c", "ls /home/mkyong/");
//
//    		// Run a shell script
//    		processBuilder.command("path/to/hello.sh");
//
//    		// -- Windows --
//
//    		// Run a command
    		builder.command("bash.exe", "-c", tempfile); //bash.exe of cygwin must be in the $PATH
//
//    		// Run a bat file
//    		processBuilder.command("C:\\Users\\mkyong\\hello.bat");
//    		builder.command(tempfile);
    		
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
			
			this.history.setHistory_end_time(BaseTool.getCurrentMySQLDatetime());
			
			this.history.setHistory_output(e.getLocalizedMessage());
			
			this.history.setIndicator(ExecutionStatus.FAILED);
			
			this.history_tool.saveHistory(this.history);
			
		}
    	
		
	}

	@Override
	public void runJupyter(String script, String processid, boolean isjoin, String bin, String env, String basedir,
			String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runPython(String script, String processid, boolean isjoin, String bin, String pyenv, String basedir,
			String token) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runMultipleBashes(String[] script, String processid) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void saveHistory(String logs, String status) {
		
		this.history.setHistory_output(logs);
		
		this.history.setIndicator(status);
		
		history_tool.saveHistory(history);
		
	}

	@Override
	public boolean stop() {
		
		if(!BaseTool.isNull(process)) {
			
			process.destroy();
			
		}
		
		if(!BaseTool.isNull(thread)) {
			
			thread.interrupt();
			
		}
		
		if(!BaseTool.isNull(input)) {
			
			try {
				input.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
		return true;
		
	}

	

	

}
