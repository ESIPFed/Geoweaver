package edu.gmu.csiss.earthcube.cyberconnector.ssh;
/*

The MIT License (MIT)

Copyright (c) 2013 The Authors

Permission is hereby granted, free of charge, to any person obtaining a copy of
this software and associated documentation files (the "Software"), to deal in
the Software without restriction, including without limitation the rights to
use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
the Software, and to permit persons to whom the Software is furnished to do so,
subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Calendar;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.connection.channel.direct.Session.Command;
import net.schmizz.sshj.connection.channel.direct.Session.Shell;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.socket.WebSocketSession;

import edu.gmu.csiss.earthcube.cyberconnector.database.DataBaseOperation;
import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.utils.RandomString;

/**
 * Geoweaver SSH session wrapper
 * @author JensenSun
 *
 */
public class SSHSessionImpl implements SSHSession {

    protected final Logger   log = LoggerFactory.getLogger(getClass());

    private SSHClient        ssh;
    
    private String 			 hostid;
    
    private Session          session; //sshj session
    
    private Shell            shell;
    
    private String           username;
    
    private String			 token; //add by Ziheng on 12 Sep 2018 - token of each execution
    
    private BufferedReader   input;
    
    private OutputStream     output;

    private SSHSessionOutput sender;
    
    private Thread           thread;
    
    private String           host;
    
    private String           port;
    
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
    
    public String getHistory_process() {
		return history_process;
	}

	public void setHistory_process(String history_process) {
		this.history_process = history_process;
	}

	public String getHistory_id() {
		return history_id;
	}

	public void setHistory_id(String history_id) {
		this.history_id = history_id;
	}
	
    public SSHClient getSsh() {
		return ssh;
	}
    
	public Session getSSHJSession() {
		return session;
	}
    
	public void setSSHJSession(Session session) {
		this.session = session;
	}

	public String getUsername() {
		return username;
	}

	public String getToken() {
		return token;
	}

	public String getHost() {
		return host;
	}

	public String getPort() {
		return port;
	}
	
	public boolean login(String hostid, String password, String token, boolean isShell) {
		
		this.hostid = hostid;
		
		String[] hostdetails = HostTool.getHostDetailsById(hostid);
		
		return this.login(hostdetails[1], hostdetails[2], hostdetails[3], password, token, false);
		
	}

	@Override
    public boolean login(String host, String port, String username, String password, String token, boolean isShell) throws AuthenticationException {
        try {
            logout();
            // ssh.authPublickey(System.getProperty("user.name"));
            log.info("new SSHClient");
            ssh = new SSHClient();
            log.info("verify all hosts");
            ssh.addHostKeyVerifier(new HostKeyVerifier() {
                public boolean verify(String arg0, int arg1, PublicKey arg2) {
                    return true; // don't bother verifying
                }
            });
            log.info("connecting");
            ssh.connect(host, Integer.parseInt(port));
            log.info("authenticating: {}", username);
            ssh.authPassword(username, password);
            log.info("starting session");
            session = ssh.startSession();
            log.info("allocating PTY");
            session.allocateDefaultPTY(); 
            
            this.username = username;
            this.token = token;
            
            if(isShell) {
            	//shell
            	log.info("starting shell");
                shell = session.startShell(); //if shell is null, it is in command mode.
                log.info("SSH session established");
                
                input = new BufferedReader(new InputStreamReader(shell.getInputStream()));
                output = shell.getOutputStream();
                sender = new SSHSessionOutput(input, token);
                //moved here on 10/29/2018
                //all SSH sessions must have a output thread
                thread = new Thread(sender);
                thread.setName("SSH output thread");
                log.info("starting sending thread");
                thread.start();
            }else {
            	//command
            	//do nothing here
            	
            }
            
        } catch (Exception e) {
        	e.printStackTrace();
            log.error(e.getMessage());
            finalize();
            throw new SSHAuthenticationException(e.getMessage(), e);
        }
        return true;

    }

    @Override
    public BufferedReader getSSHInput() {
        return input;
    }

    @Override
    public OutputStream getSSHOutput() {
        return output;
    }

    @Override
    protected void finalize() {
    	//stop the thread first
        try {
        	if(sender!=null)
        		sender.stop();
//        	thread.interrupt();
        } catch (Throwable e) {
        	e.printStackTrace();
        }
        try {
            shell.close();
        } catch (Throwable e) {
        }
        try {
            session.close();
        } catch (Throwable e) {
        }
        try {
            ssh.disconnect();
            
        } catch (Throwable e) {
        }
        
        log.info("session finalized");
    }
    
    
    
    @Override
	public void saveHistory(String logs) {
		
    	this.history_end_time = BaseTool.getCurrentMySQLDatetime();
    	
    	this.history_output = logs;
    	
    	StringBuffer sql = new StringBuffer("insert into history (id, process, begin_time, end_time, input, output, host) values (\"");
    	
    	sql.append(this.history_id).append("\",\"");
    	
    	sql.append(this.history_process).append("\",\"");
    	
    	sql.append(this.history_begin_time).append("\",\"");
    	
    	sql.append(this.history_end_time).append("\",?, ?, \"");
    	
    	sql.append(this.hostid).append("\" )");
    	
    	DataBaseOperation.preexecute(sql.toString(), new String[] {this.history_input, this.history_output});
    	
	}

	@Override
	public void runBash(String script, String processid, boolean isjoin) {
    	
		this.history_id = new RandomString(12).nextString();
		
		this.history_process = processid.split("-")[0]; //only retain process id, remove object id
		
		this.history_begin_time = BaseTool.getCurrentMySQLDatetime();
		
		this.history_input = script;
    	
    	try {
    		
    		log.info("starting command");
    		
    		String cmdline = "echo \"" + script.replaceAll("\r\n", "\n").replaceAll("\"", "\\\\\"") + "\" > geoweaver-" + token + ".sh; ";
    		
    		cmdline += "chmod +x geoweaver-" + token + ".sh; ";
    		
    		cmdline += "./geoweaver-" + token + ".sh; ";
    		
    		cmdline += "echo \"==== Geoweaver Bash Output Finished ====\"; ";
			
    		log.info(cmdline);
    		
    		Command cmd = session.exec(cmdline);
//            con.writer().print(IOUtils.readFully(cmd.getInputStream()).toString());
//            cmd.join(5, TimeUnit.SECONDS);
//            con.writer().print("\n** exit status: " + cmd.getExitStatus());
    		
            log.info("SSH command session established");
            
            input = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            
            sender = new SSHSessionOutput(input, token);
            
            //moved here on 10/29/2018
            //all SSH sessions must have a output thread
            
            thread = new Thread(sender);
            
            thread.setName("SSH output thread");
            
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
    	
    	//feed the process code into the SSH session
    	

//		
//		Session.Command cmd = session.getSSHJSession().exec(executebash);
//		
//		String output = IOUtils.readFully(cmd.getInputStream()).toString();
//		
//		logger.info(output);
//		
//		//wait until the process execution is over
//		
//        cmd.join(5, TimeUnit.SECONDS);
//        
//		cmd.close();
//		
//		session.logout();
	}

	@Override
	public void runMultipleBashes(String[] script, String processid) {
		// TODO Auto-generated method stub
		
		
	}

	@Override
    public void setWebSocketSession(WebSocketSession session) {
        this.sender.setWebSocketSession(session); //connect WebSocket with SSH output thread
    }

    @Override
    public boolean logout() {
        log.info("logout: {}", username);
        try {
            // output.write("exit".getBytes());
            finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return true;
    }

}
