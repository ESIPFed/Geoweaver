package com.gw.local;

import java.io.BufferedReader;
import java.io.IOException;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.gw.server.CommandServlet;
import com.gw.tools.LocalhostTool;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

@Service
public class LocalSessionOutput  implements Runnable{

	@Autowired
	BaseTool bt;
	
	@Autowired
	LocalhostTool lt;
	
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected BufferedReader in;
    
    protected WebSocketSession out; // log&shell websocket - not used any more
    
    protected Session wsout;
    
    protected String token; // session token
    
    protected boolean run = true;
    
    protected String history_id;
    
    
    
    public LocalSessionOutput() {
    	//this is for spring
    }
    
    public void init(BufferedReader in, String token, String history_id) {
        log.info("created");
        this.in = in;
        this.token = token;
        this.run = true;
		this.history_id = history_id;
		
        refreshLogMonitor();
    }
    
    
    public void stop() {
    	
    	run = false;
    	
    }

	public void sendMessage2WebSocket(String msg){

		synchronized(wsout){

			try {
				if(!bt.isNull(wsout) && wsout.isOpen())
					wsout.getBasicRemote().sendText(msg);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}

	}

	public void refreshLogMonitor(){

		if(bt.isNull(wsout)){

			wsout = CommandServlet.findSessionById(token);

			if(bt.isNull(wsout)){

				wsout = CommandServlet.findSessionById(history_id);
			}

		}
	}
    
    @Override
    public void run() {
        
		try{
			log.info("Local session output thread started");
		
			StringBuffer prelog = new StringBuffer(); //the part that is generated before the WebSocket session is started
			
			StringBuffer logs = new StringBuffer();
			
			int linenumber = 0;
			
			int startrecorder = -1;
			
			int nullnumber = 0;
			
			LocalSession session = lt.getLocalSession();//GeoweaverController.sessionManager.localSessionByToken.get(token);
			
			if(!bt.isNull(session))session.saveHistory("Running", "Running"); //initiate the history record

			sendMessage2WebSocket("Process "+this.history_id+" Started");
			
			while (run) {

				try {

					refreshLogMonitor();
					
					// readLine will block if nothing to send
					
					if(bt.isNull(in)) { 
					
						log.debug("Local Session Output Reader is close prematurely.");
						
						break;
						
					}
					
					String line = in.readLine();
					
					linenumber++;
					
					//when detected the command is finished, end this process
					if(bt.isNull(line)) {
						
						//if ten consective output lines are null, break this loop
						
						if(startrecorder==-1) 
							startrecorder = linenumber;
						else
							nullnumber++;
						
						if(nullnumber==10) {
							
							if((startrecorder+nullnumber)==linenumber) {
								
								log.debug("null output lines exceed 10. Disconnected.");
								
								if(!bt.isNull(session)) 
									
									session.saveHistory(logs.toString(), "Done");
								
								break;
								
							}else {
								
								startrecorder = -1;
								
								nullnumber = 0;
								
							}
							
						}
						
					}else if(line.contains("==== Geoweaver Bash Output Finished ====")) {
						
	//                	session.saveHistory(logs.toString()); //complete the record
						
						if(!bt.isNull(session)) session.saveHistory(logs.toString(), "Done");
						
						sendMessage2WebSocket("The process "+session.getHistory().getHistory_id()+" is finished.");
							
						break;
						
					}else {
						
						log.info("Local thread output >> " + line);
						
						logs.append(line).append("\n");
						
						if(!bt.isNull(wsout) && wsout.isOpen()) {
							
							if(prelog.toString()!=null) {
								
								line = prelog.toString() + line;
								
								prelog = new StringBuffer();
								
							}
							
							this.sendMessage2WebSocket(line);
							
						}else {
							
							prelog.append(line).append("\n");
							
						}
						
					}
					
				} catch (Exception e) {
					
					e.printStackTrace();
					
					
					if(!bt.isNull(session)) 
						
						session.saveHistory(logs.toString(), "Failed");

					break;
					
				}finally {
					
	//                session.saveHistory(logs.toString()); //write the failed record
					
				}
				
			}
			
			//this thread will end by itself when the task is finished, you don't have to close it manually

			GeoweaverController.sessionManager.closeByToken(token);
			
			log.info("Local session output thread ended");

		}catch(Exception e){

			e.printStackTrace();
		
		}finally{
			
						
			sendMessage2WebSocket("Process " + this.history_id + " ended");
			

		}

    }
    

}
