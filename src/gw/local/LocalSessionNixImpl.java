package gw.local;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.WebSocketSession;

import gw.log.History;
import gw.ssh.SSHCmdSessionOutput;
import gw.ssh.SSHSessionOutput;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
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
    
    private String           tempfile;


//	@Override
//	public void setWebSocketSession(WebSocketSession session) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void runBash(String script, String processid, boolean isjoin, String token) {
		
//		this.history_id = token; //new RandomString(12).nextString();
		
		this.history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
		
		this.history.setHistory_begin_time(BaseTool.getCurrentMySQLDatetime());
		
		this.history.setHistory_input(script);
    	
    	try {
    		
    		log.info("starting command");

    		String rand = new RandomString(3).nextString();
    		
    		tempfile = SysDir.workspace + "/gw-" + token + "-" + rand + ".sh";

    		script += "\n echo \"==== Geoweaver Bash Output Finished ====\"";
    		
    		BaseTool.writeString2File(script, tempfile);
    		
    		ProcessBuilder builder = new ProcessBuilder();
    		
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
	

}
