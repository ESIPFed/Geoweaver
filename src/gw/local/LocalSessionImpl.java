package gw.local;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.WebSocketSession;

import gw.ssh.SSHCmdSessionOutput;
import gw.ssh.SSHSessionOutput;
import gw.utils.BaseTool;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;

public class LocalSessionImpl implements LocalSession {

	Logger log  = Logger.getLogger(this.getClass());
	
	private boolean			 isTerminal;
	
	private BufferedReader   input;
	    
    private OutputStream     output;

    private LocalSessionOutput sender;
    
    private Thread           thread;
    
    /**********************************************/
    /** section of the geoweaver history records **/
    /**********************************************/
    private String			 history_input;
    
    private String			 history_output;
    
    private String			 history_begin_time;
    
    private String			 history_end_time;
    
    private String			 history_process;
    
    private String			 history_id;
    
    /**********************************************/
    /** end of history section **/
    /**********************************************/
	
	
	@Override
	public boolean login(String token, boolean isTerminal) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean logout() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public String getHistory_process() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHistory_process(String history_process) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getHistory_id() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setHistory_id(String history_id) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void setWebSocketSession(WebSocketSession session) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void runBash(String script, String processid, boolean isjoin, String token) {
		
//		this.history_id = token; //new RandomString(12).nextString();
		
		this.history_process = processid.split("-")[0]; //only retain process id, remove object id
		
		this.history_begin_time = BaseTool.getCurrentMySQLDatetime();
		
		this.history_input = script;
    	
    	try {
    		
    		log.info("starting command");
    		
    		script += "\n echo \"==== Geoweaver Bash Output Finished ====\"";
    		
    		String cmdline = "echo \"" 
    				+ script.replaceAll("\r\n", "\n").replaceAll("\\$", "\\\\\\$").replaceAll("\"", "\\\\\"") 
    				+ "\" > geoweaver-" + token + ".sh; ";
    		
    		cmdline += "chmod +x geoweaver-" + token + ".sh; ";
    		
    		cmdline += "./geoweaver-" + token + ".sh;";
    		
    		cmdline += "rm ./geoweaver-" + token + ".sh; "; //remove the script finally, leave no trace behind
			
    		log.info(cmdline);
    		
    		ProcessBuilder builder = new ProcessBuilder(cmdline);
    		
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
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getToken() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getHost() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getPort() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isTerminal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public BufferedReader getLocalInput() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public OutputStream getLocalOutput() {
		// TODO Auto-generated method stub
		return null;
	}
	
	

}
