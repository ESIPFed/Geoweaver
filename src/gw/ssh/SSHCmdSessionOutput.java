package gw.ssh;

import java.io.BufferedReader;

import org.springframework.web.socket.WebSocketSession;

import gw.utils.BaseTool;
import gw.web.GeoweaverController;
import gw.ws.server.CommandServlet;
/**
 * This class is for command line output
 * 
 * @author JensenSun
 *
 */
public class SSHCmdSessionOutput  extends SSHSessionOutput {

	
    public SSHCmdSessionOutput(BufferedReader in, String token) {
    	
    	super(in, token);
    	
    	wsout = CommandServlet.findSessionById(token);
    	
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
    	
    	SSHSession session = GeoweaverController.sessionManager.sshSessionByToken.get(token);
    	
    	if(!BaseTool.isNull(session))session.saveHistory("Running", "Running"); //initiate the history record
    	
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
                			
                			System.out.println("null output lines exceed 10. Disconnected.");
                			
                			if(!BaseTool.isNull(session)) 
                				
                				session.saveHistory(logs.toString(), "Done");
                			
                			break;
                			
                		}else {
                			
                			startrecorder = -1;
                			
                			nullnumber = 0;
                			
                		}
                		
                	}
                	
                }else if(line.contains("==== Geoweaver Bash Output Finished ====")) {
                	
//                	session.saveHistory(logs.toString()); //complete the record
                	
                	if(!BaseTool.isNull(session)) session.saveHistory(logs.toString(), "Done");
                	
                	if(!BaseTool.isNull(wsout) && wsout.isOpen())
                		wsout.getBasicRemote().sendText("The process "+session.getHistory_id()+" is finished.");
                	
                	break;
                	
                }
                
                log.info("command thread output >> " + line);
                
                logs.append(line).append("\n");
                
                if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
                	
                	if(prelog.toString()!=null) {
                		
                		line = prelog.toString() + line;
                		
                		prelog = new StringBuffer();
                		
                	}
                	
                	log.info("wsout message {}:{}", wsout.getId(), line);
                	
//                    out.sendMessage(new TextMessage(line));
                	wsout.getBasicRemote().sendText(line);
                    
                }else {
                	
                	prelog.append(line).append("\n");
                	
                }
                
            } catch (Exception e) {
            	
                e.printStackTrace();
                
                if(!BaseTool.isNull(session)) session.saveHistory(logs.toString(), "Failed");
                
            }finally {
            	
//                session.saveHistory(logs.toString()); //write the failed record
                
            }
            
        }
        
        GeoweaverController.sessionManager.closeByToken(token);
        
        log.info("SSH session output thread ended");

    }
    
    public void setWebSocketSession(WebSocketSession session) {
        log.info("received websocket session");
//        this.out = session;
    }
    
}
