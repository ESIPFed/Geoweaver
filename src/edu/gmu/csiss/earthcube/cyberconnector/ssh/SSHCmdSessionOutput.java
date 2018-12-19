package edu.gmu.csiss.earthcube.cyberconnector.ssh;

import java.io.BufferedReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.web.GeoweaverController;

public class SSHCmdSessionOutput  extends SSHSessionOutput {

	
    public SSHCmdSessionOutput(BufferedReader in, String token) {
    	
    	super(in, token);
    	
    }
    
    public void stop() {
    	
    	run = false;
    	
    }
    
    @Override
    public void run() {
        
    	log.info("SSH session output thread started");
    	
    	StringBuffer prelog = new StringBuffer(); //the part that is generated before the WebSocket session is started
        
    	StringBuffer logs = new StringBuffer();
    	
    	int linenumber = 0;
    	
    	int startrecorder = -1;
    	
    	int nullnumber = 0;
    	
        while (run) {
        	
            try {
                
            	// readLine will block if nothing to send
            	
                String line = in.readLine();
                
                linenumber++;
                
                //when detected the command is finished, end this process
                if(BaseTool.isNull(line)) {
                	
                	//if ten consective output lines are null, break this loop
                	
                	if(startrecorder==-1) 
                		startrecorder = linenumber;
                	else
                		nullnumber++;
                	
                	if(nullnumber==10) {
                		
                		if((startrecorder+nullnumber)==linenumber) {
                			
                			System.out.println("null output lines exceed 100. Disconnected.");
                			
                			GeoweaverController.sshSessionManager.closeByToken(token);
                		
                			break;
                			
                		}else {
                			
                			startrecorder = -1;
                			
                			nullnumber = 0;
                			
                		}
                		
                	}
                	
                }else if(line.contains("==== Geoweaver Bash Output Finished ====")) {
                	
                	SSHSession session = GeoweaverController.sshSessionManager.sessionsByToken.get(token);
                	
                	session.saveHistory(logs.toString());
                	
                	GeoweaverController.sshSessionManager.closeByToken(token);
                	
                	break;
                	
                }
                
//                System.out.println("thread output >> " + line);
                
                logs.append(line).append("\n");
                
                if(!BaseTool.isNull(out)) {
                	
                	if(prelog.toString()!=null) {
                		
                		line = prelog.toString() + line;
                		
                		prelog = new StringBuffer();
                		
                	}
                	
//                    log.info("message out {}:{}", out.getId(), line);
                    
                    out.sendMessage(new TextMessage(line));
                    
                }else {
                	
                	prelog.append(line).append("\n");
                	
                }
                
            } catch (Exception e) {
            	
                e.printStackTrace();
                
                GeoweaverController.sshSessionManager.closeByToken(token);
                
            }
            
        }
        
        log.info("SSH session output thread ended");

    }
    
    public void setWebSocketSession(WebSocketSession session) {
        log.info("received websocket session");
        this.out = session;
    }
    
}
