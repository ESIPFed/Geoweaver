package com.gw.ssh;

import java.io.BufferedReader;

import javax.websocket.Session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.History;
import  com.gw.server.CommandServlet;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;
/**
 * This class is for command line output
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
//@Qualifier("SSHCmdSessionOutput")
public class SSHCmdSessionOutput  implements Runnable {
	
	protected Logger log = LoggerFactory.getLogger(getClass());

    protected BufferedReader in;
    
    protected WebSocketSession out; // log&shell websocket - not used any more
    
    protected Session wsout;
    
    protected String token; // session token
    
    protected boolean run = true;
    
    protected String history_id;

	@Autowired
	BaseTool bt;

	@Autowired
	HistoryTool ht;
	
	public SSHCmdSessionOutput() {
		
		//for spring
		
	}
	
    
	
    public void init(BufferedReader in, String token, String history_id) {
    	
    	log.info("created");
        this.in = in;
        this.token = token;
        this.run = true;
		this.history_id = history_id;
    	wsout = CommandServlet.findSessionById(token);
    	
    }
    
    public void stop() {
    	
    	run = false;
    	
    }

	/**
	 * End process with exit code
	 * @param token
	 * @param exitvalue
	 */
	public void endWithCode(String token, String history_id, int exitvalue){

		History h = ht.getHistoryById(history_id); 
		
		if(exitvalue == 0){
			
			h.setIndicator(ExecutionStatus.DONE);

		}else{

			h.setIndicator(ExecutionStatus.FAILED);

		}

		h.setHistory_end_time(BaseTool.getCurrentSQLDate());
		
		ht.saveHistory(h);

        log.info("Exit code: " + exitvalue);

		CommandServlet.sendMessageToSocket(token, "Exit Code: " + exitvalue);

	}

	public void updateStatus(String logs, String status){

		History h = ht.getHistoryById(this.history_id);

		if(BaseTool.isNull(h)){

			h = new History();

			h.setHistory_id(history_id);

			log.debug("This is very unlikely");
		}

		if(ExecutionStatus.DONE.equals(status) || ExecutionStatus.FAILED.equals(status) 
				|| ExecutionStatus.STOPPED.equals(status)){

			h.setHistory_end_time(BaseTool.getCurrentSQLDate());

		}

		h.setHistory_output(logs);

		h.setIndicator(status);

		ht.saveHistory(h);

	}
    
    @Override
    public void run() {
        
    	log.info("SSH session output thread started");
    	
    	StringBuffer prelog = new StringBuffer(); //the part that is generated before the WebSocket session is started
        
    	StringBuffer logs = new StringBuffer();
    	
    	int linenumber = 0;
    	
    	int startrecorder = -1;
    	
    	int nullnumber = 0;
    	
    	// SSHSession session = GeoweaverController.sessionManager.sshSessionByToken.get(token);
    	
    	updateStatus("Running", "Running"); //initiate the history record

		sendMessage2WebSocket("Process "+this.history_id+" Started");
    	
		String line = null;
        // while (run) {
			
		try {

			while ((line = in.readLine())!=null){
				
				// readLine will block if nothing to send
				
				// String line = in.readLine();
				
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
							
							log.debug("null output lines exceed 10. Disconnected.");
							
							// this.updateStatus(logs.toString(), "Done");
							
							break;
							
						}else {
							
							startrecorder = -1;
							
							nullnumber = 0;
							
						}
						
					}
					
				}
				
				log.info("command thread output >> " + line);
				
				logs.append(line).append("\n");

				if(linenumber%1==0){
					this.updateStatus(logs.toString(), "Running");
				}
				
				if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
					
					if(prelog.toString()!=null) {
						
						line = prelog.toString() + line;
						
						prelog = new StringBuffer();
						
					}
					
					sendMessage2WebSocket(line);
					
				}else {
					
					prelog.append(line).append("\n");
					
				}
				
			}

			this.updateStatus(logs.toString(), "Done");
			
			sendMessage2WebSocket("The process "+this.history_id+" is finished.");
			

		} catch (Exception e) {
					
			e.printStackTrace();
			
			updateStatus(logs.toString(), "Failed");
			
		}finally {
			
			sendMessage2WebSocket("======= Process " + this.history_id + " ended");
	//                session.saveHistory(logs.toString()); //write the failed record
			
		}
        
        GeoweaverController.sessionManager.closeByToken(token);
        
        log.info("SSH session output thread ended");

    }

	public void sendMessage2WebSocket(String msg){

		if(!BaseTool.isNull(wsout)){
		
			synchronized(wsout){

				try {
					if(wsout.isOpen())
						wsout.getBasicRemote().sendText(msg);
				} catch (Exception e) {
					e.printStackTrace();
				}
	
			}
		}
		

	}
    
    public void setWebSocketSession(WebSocketSession session) {
        log.info("received websocket session");
//        this.out = session;
    }
    
}
