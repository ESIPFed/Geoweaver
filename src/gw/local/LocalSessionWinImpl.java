package gw.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import javax.websocket.Session;

import org.apache.log4j.Logger;

import gw.log.ExecutionStatus;
import gw.log.History;
import gw.tools.HistoryTool;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;
import gw.ws.server.CommandServlet;

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
		
		this.token = token;
		
		this.isTerminal = isjoin;
		
		this.history.setHistory_id(new RandomString(12).nextString());
		
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
			
			try {
			
				Session wsout = CommandServlet.findSessionById(token);
				
				if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
					
					log.info("The failed message has been sent to client");
					
					wsout.getBasicRemote().sendText(e.getLocalizedMessage());
					
					wsout.getBasicRemote().sendText("The process " + this.history.getHistory_id() + " is stopped.");
					
				}
				
			} catch (IOException e1) {
				
				e1.printStackTrace();
				
			}
			
			this.stop();
			
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
	public void runPython(String python, String processid, boolean isjoin, String bin, String pyenv, String basedir,
			String token) {
		
		history = history_tool.initProcessHistory(history, processid, python);
		
    	try {
    		
    		log.info("save to local file: " + python);
    		
    		tempfile = SysDir.workspace + "/gw-" + token + "-" + history.getHistory_id() + ".py";

    		BaseTool.writeString2File(python, tempfile);
    		
    		
    		
//    		python = escapeJupter(python);
    		
//    		log.info("escaped command: " + python);
    		
//    		python = python.replaceAll("\\\n", ".");
//    		python = python.replace("\\n", "\\\\n");
    		
//    		String cmdline = "";
//    		
//    		if(!BaseTool.isNull(basedir)||"default".equals(basedir)) {
//    			
//    			cmdline += "cd \"" + basedir + "\"; ";
//    			
//    		}
    		
    		//new version of execution in which all the python files are copied in the host
    		
//    		cmdline += "mkdir " + token + ";";
//    		
//    		cmdline += "tar -xvf " + token + ".tar -C " + token + "/; ";
//    		
//    		cmdline += "cd "+ token + "/; ";
//    		
////    		cmdline += "printf \"" + python + "\" > python-" + history_id + ".py; ";
//    		
//    		cmdline += "chmod +x *.py;";
    		
//    		String filename = ProcessTool.getNameById(processid);
//    		
//    		filename = filename.trim().endsWith(".py")? filename: filename+".py";
//    		
//    		if(BaseTool.isNull(bin)||"default".equals(bin)) {
//
////    			cmdline += "python python-" + history_id + ".py;";
//    			cmdline += "python " + filename + "; ";
//    			
//    		}else {
//    			
////    			cmdline += "conda init; ";
//    			
//    			cmdline += "source activate " + pyenv + "; "; //for demo only
//    			
//    			cmdline += bin + " " + filename + "; ";
//    			
//    		}
//    		
//    		cmdline += "echo \"==== Geoweaver Bash Output Finished ====\"";
//    		
//    		cmdline += "cd ..; rm -R " + token + "*;";
//    		
//    		log.info(cmdline);
//    		
//    		Command cmd = session.exec(cmdline);
    		
//            log.info("SSH command session established");
            
//            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            
            sender = new LocalSessionOutput(input, token);
            
            //moved here on 10/29/2018
            //all SSH sessions must have a output thread
            
            thread = new Thread(sender);
            
            thread.setName("SSH Command output thread");
            
            log.info("starting sending thread");
            
            thread.start();
            
            log.info("returning to the client..");
            
            if(isjoin) thread.join(7*24*60*60*1000); //longest waiting time - a week
//	        
//	        output.write((cmd + '\n').getBytes());
//			
////	        output.flush();
//	        
//	        cmd = "./geoweaver-" + token + ".sh";
//	        		
//	        output.write((cmd + '\n').getBytes());
//			
////	        output.flush();
//	        	
//	        cmd = "echo \"==== Geoweaver Bash Output Finished ====\"";
//	        
//	        output.write((cmd + '\n').getBytes());
//	        output.flush();
	        
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
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

	@Override
	public boolean clean() {
		
		File temp = new File(tempfile);
		
		return temp.delete();
		
	}

	

	

}
