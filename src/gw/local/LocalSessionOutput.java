package gw.local;

import java.io.BufferedReader;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.WebSocketSession;

import gw.ssh.SSHSession;
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
    	
    	LocalSession session = GeoweaverController.sshSessionManager.localSessionByToken.get(token);
    	
    	if(!BaseTool.isNull(session))session.saveHistory("Running", "Running"); //initiate the history record
    	
        while (run) {
        	
            try {
                
            	// readLine will block if nothing to send
            	
            	if(BaseTool.isNull(in)) break;
            	
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
                		wsout.getBasicRemote().sendText("The process "+session.getHistory().getHistory_id()+" is finished.");
                	
                	break;
                	
                }else {
                	
                	log.info("Local thread output >> " + line);
                    
                    logs.append(line).append("\n");
                    
                    if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
                    	
                    	if(prelog.toString()!=null) {
                    		
                    		line = prelog.toString() + line;
                    		
                    		prelog = new StringBuffer();
                    		
                    	}
                    	
//                    	log.info("wsout message {}:{}", wsout.getId(), line);
                    	
//                        out.sendMessage(new TextMessage(line));
                    	wsout.getBasicRemote().sendText(line);
                        
                    }else {
                    	
                    	prelog.append(line).append("\n");
                    	
                    }
                	
                }
                
            } catch (Exception e) {
            	
                e.printStackTrace();
                
                if(!BaseTool.isNull(session)) 
                	
                	session.saveHistory(logs.toString(), "Failed");
                
            }finally {
            	
//                session.saveHistory(logs.toString()); //write the failed record
                
            }
            
        }
        
        GeoweaverController.sshSessionManager.closeByToken(token);
        
        log.info("Local session output thread ended");

    }
    

}
