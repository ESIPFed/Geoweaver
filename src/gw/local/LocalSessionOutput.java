package gw.local;

import java.io.BufferedReader;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import gw.utils.BaseTool;
import gw.web.GeoweaverController;
import gw.ws.server.CommandServlet;
import gw.ws.server.TerminalServlet;

public class LocalSessionOutput  implements Runnable{

    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected final BufferedReader in;
    
    protected WebSocketSession out; // log&shell websocket - not used any more
    
    protected Session wsout;
    
    protected String token; // session token
    
    protected boolean run = true;
    
    protected String history_id;
    
    public LocalSessionOutput(BufferedReader in, String token) {
        log.info("created");
        this.in = in;
        this.token = token;
        wsout = CommandServlet.findSessionById(token);
    }
    
    public void stop() {
    	
    	run = false;
    	
    }
    
    @Override
    public void run() {
        
    	log.info("Local session output thread started");
    	
    	StringBuffer prelog = new StringBuffer(); //the part that is generated before the WebSocket session is started
        
    	StringBuffer logs = new StringBuffer();
    	
    	int linenumber = 0;
    	
    	int startrecorder = -1;
    	
    	int nullnumber = 0;
    	
        while (run) {
        	
            try {
                
            	// readLine will block if nothing to send
            	
                String line = in.readLine();
                
//                out.sendMessage(new TextMessage(line));
                
//                linenumber++;
//                
//                //when detected the command is finished, end this process
//                if(BaseTool.isNull(line)) {
//                	
//                	//if ten consective output lines are null, break this loop
//                	
//                	if(startrecorder==-1) 
//                		startrecorder = linenumber;
//                	else
//                		nullnumber++;
//                	
//                	if(nullnumber==10) {
//                		
//                		if((startrecorder+nullnumber)==linenumber) {
//                			
//                			System.out.println("null output lines exceed 100. Disconnected.");
//                			
//                			GeoweaverController.sshSessionManager.closeByToken(token);
//                		
//                			break;
//                			
//                		}else {
//                			
//                			startrecorder = -1;
//                			
//                			nullnumber = 0;
//                			
//                		}
//                		
//                	}
//                	
//                }else if(line.contains("==== Geoweaver Bash Output Finished ====")) {
//                	
//                	SSHSession session = GeoweaverController.sshSessionManager.sshSessionByToken.get(token);
//                	
//                	session.saveHistory(logs.toString());
//                	
//                	GeoweaverController.sshSessionManager.closeByToken(token);
//                	
//                	break;
//                	
//                }
//                
                log.info("shell thread output >> " + line);
//                
//                logs.append(line).append("\n");
                
                if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
//                	
//                	if(prelog.toString()!=null) {
//                		
//                		line = prelog.toString() + line;
//                		
//                		prelog = new StringBuffer();
//                		
//                	}
                	
                    log.info("wsout message {}:{}", token, line);
                    
//                    out.sendMessage(new TextMessage(line)); // for the SockJS session to deliver it back to the SSH Terminal
                    
                	wsout.getBasicRemote().sendText(line); // for the All information web socket session
                    
//                }else {
//                	
//                	prelog.append(line).append("\n");
//                	
                }else {
                	
                	wsout = TerminalServlet.findSessionById(token);
                	
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
//        this.out = session;
    }
    
	

}
