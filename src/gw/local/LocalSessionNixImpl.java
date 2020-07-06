package gw.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import org.apache.log4j.Logger;

import gw.log.History;
import gw.ssh.SSHCmdSessionOutput;
import gw.tools.HistoryTool;
import gw.tools.ProcessTool;
import gw.utils.BaseTool;
import gw.utils.RandomString;
import gw.utils.SysDir;
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


//	@Override
//	public void setWebSocketSession(WebSocketSession session) {
//		// TODO Auto-generated method stub
//		
//	}

	@Override
	public void runBash(String script, String processid, boolean isjoin, String token) {
		
//		this.history_id = token; //new RandomString(12).nextString();
		
		this.history = history_tool.initProcessHistory(history, processid, script);
    	
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
			
		}
    	
		
	}

	@Override
	public void runJupyter(String notebookjson, String processid, boolean isjoin, String bin, String env, String basedir,
			String token) {
		
		history = history_tool.initProcessHistory(history, processid, notebookjson);

//		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
//		
//		history.setHistory_begin_time(BaseTool.getCurrentMySQLDatetime());
//		
//		history.setHistory_input(notebookjson);
    	
    	try {
    		
    		log.info("starting command");
    		
//    		String cmdline = "";
//    		
//    		if(!BaseTool.isNull(basedir)||"default".equals(basedir)) {
//    			
//    			cmdline += "cd \"" + basedir + "\"; ";
//    			
//    		}
//    		
//    		notebookjson = escapeJupter(notebookjson);
//    		
//    		cmdline += "echo \"" + notebookjson + "\" > jupyter-" + history.getHistory_id() + ".ipynb; ";
    		
//    		if(!(BaseTool.isNull(bin)||"default".equals(bin))) {
//    			
//    			cmdline += "source activate " + pyenv + "; ";
//    			
//    		}
    		tempfile = SysDir.workspace + "/gw-" + token + "-" + history.getHistory_id() + ".ipynb";

    		BaseTool.writeString2File(notebookjson, tempfile);
    		
    		if(BaseTool.isNull(bin)||"default".equals(bin)) {

//    			cmdline += "python python-" + history_id + ".py;";
    			
//    			cmdline += "python " + filename + "; ";
    			
    		}else {
    			
//    			cmdline += "conda init; ";
    			
//    			cmdline += "source activate " + env + "; "; //for demo only
    			
    			Runtime.getRuntime().exec(new String[] {"source", "activate", env});
    			
//    			cmdline += bin + " " + filename + "; ";
    			
    		}
    		
//    		cmdline += "jupyter nbconvert --to notebook --execute jupyter-" + history.getHistory_id() + ".ipynb;";
    		
//    		cmdline += "rm ./jupyter-" + history.getHistory_id() + ".ipynb; "; // remove the script finally, leave no trace behind
    		
//    		cmdline += "echo \"==== Geoweaver Bash Output Finished ====\"";
    		
//    		cmdline += "./geoweaver-" + token + ".sh;";
    		
//    		cmdline += "cat ./jupyter-"+token+".ipynb | while read line\r\n" + 
//    				"do\r\n" + 
//    				"  echo \"$line\"\r\n" + 
//    				"done; "; // read the content of the result ipynb
    		
			
//    		log.info(cmdline);
    		
//    		Command cmd = session.exec(cmdline);
//            con.writer().print(IOUtils.readFully(cmd.getInputStream()).toString());
//            cmd.join(5, TimeUnit.SECONDS);
//            con.writer().print("\n** exit status: " + cmd.getExitStatus());
    		
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
	public void runPython(String python, String processid, boolean isjoin, String bin, String pyenv, String basedir,
			String token) {
		

//		this.history_id = token; //new RandomString(12).nextString();
		
		history = history_tool.initProcessHistory(history, processid, python);
		
//		history.setHistory_process(processid.split("-")[0]); //only retain process id, remove object id
//		
//		history.setHistory_begin_time(BaseTool.getCurrentMySQLDatetime());
//		
//		history.setHistory_input(python);
    	
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
		
		
		
	}

	@Override
	public void saveHistory(String logs, String status) {
		
		history.setHistory_output(logs);
		history.setIndicator(status);
		history_tool.saveHistory(history);
		
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
