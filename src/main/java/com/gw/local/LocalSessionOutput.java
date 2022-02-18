package com.gw.local;

import java.io.BufferedReader;

import javax.websocket.Session;

import com.gw.database.HistoryRepository;
import com.gw.jpa.History;
import com.gw.server.CommandServlet;
import com.gw.tools.HistoryTool;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

@Service
@Scope("prototype")
public class LocalSessionOutput  implements Runnable{

	@Autowired
	BaseTool bt;
	
	// @Autowired
	// LocalhostTool lt;

	@Autowired
	HistoryTool ht;

	@Autowired
	HistoryRepository historyrespository;
	
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected BufferedReader in;
    
    protected WebSocketSession out; // log&shell websocket - not used any more
    
    protected Session wsout;
    
    protected String token; // session token
    
    protected boolean run = true;
    
    protected String history_id;

	protected String lang;
    
	protected String jupyterfilepath;
    
    
    public LocalSessionOutput() {
    	//this is for spring
    }
    
    public void init(BufferedReader in, String token, String history_id, String lang, String jupyterfilepath) {
        log.info("created");
        this.in = in;
        this.token = token;
        this.run = true;
		this.history_id = history_id;
		this.lang = lang;
		this.jupyterfilepath = jupyterfilepath;
        refreshLogMonitor();
    }
    
    
    public void stop() {
    	
    	run = false;
    	
    }

	public void sendMessage2WebSocket(String msg){

		if(!bt.isNull(wsout)){

			synchronized(wsout){

				try {
			
					if(wsout.isOpen())
						wsout.getBasicRemote().sendText(msg);
					else
						log.debug("Websocket is closed, message didn't send: " + msg );
			
				} catch (Exception e) {
			
					e.printStackTrace();
					log.debug("Exception happens, message didn't send: " + msg);
			
				}
	
			}

		}else{

			log.debug("Websocket is null, message didn't send: " + msg);

		}
		

	}

	public void refreshLogMonitor(){

		if(bt.isNull(wsout) || !wsout.isOpen()){

			wsout = CommandServlet.findSessionById(token);
			
		}

	}

	public void cleanLogMonitor(){

		CommandServlet.removeSessionById(history_id);
			
	}

	public void updateJupyterStatus(String logs, String status){

		History h = ht.getHistoryById(this.history_id);

		if(bt.isNull(h)){

			h = new History();

			h.setHistory_id(history_id);

			log.debug("This is very unlikely");

		}

		String resultjupyterjson = bt.readStringFromFile(this.jupyterfilepath);

		h.setHistory_input(resultjupyterjson);

		h.setHistory_output(logs);

		h.setIndicator(status);

		if("Done".equals(status) || "Failed".equals(status)){

			h.setHistory_end_time(bt.getCurrentSQLDate());

		}

		ht.saveHistory(h);
		// historyrespository.save(h);

		// log.debug("print out history_output: " + h.getHistory_output());

	}
	
	public void updateStatus(String logs, String status){

		History h = ht.getHistoryById(this.history_id);

		if(bt.isNull(h)){

			h = new History();

			h.setHistory_id(history_id);

			log.debug("This is very unlikely");

		}

		h.setHistory_output(logs);

		h.setIndicator(status);

		if("Done".equals(status) || "Failed".equals(status)){

			h.setHistory_end_time(bt.getCurrentSQLDate());

		}

		ht.saveHistory(h);
		// historyrespository.save(h);

		// log.debug("print out history_output: " + h.getHistory_output());

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
			
			this.updateStatus("Running", "Running"); //initiate the history record

			sendMessage2WebSocket("Process "+this.history_id+" Started");
			
			String line = null;

			while((line = in.readLine()) != null){

				try {

					refreshLogMonitor();
					
					// readLine will block if nothing to send
					
					if(bt.isNull(in)) { 
					
						log.debug("Local Session Output Reader is close prematurely.");
						
						break;
						
					}
					
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
								
								if("jupyter".equals(this.lang)){

									this.updateJupyterStatus(logs.toString(), "Done");
					
								}else{
					
									this.updateStatus(logs.toString(), "Done");
					
								}
								
								break;
								
							}else {
								
								startrecorder = -1;
								
								nullnumber = 0;
								
							}
							
						}
						
					}else if(line.contains("==== Geoweaver Bash Output Finished ====")) {
						
	//                	session.saveHistory(logs.toString()); //complete the record
						
						// this.updateStatus(logs.toString(), "Done");
						
						// sendMessage2WebSocket("The process "+history_id+" is finished.");
							
						// break;
						
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
					
					if("jupyter".equals(this.lang)){

						this.updateJupyterStatus(logs.toString(), "Failed");
		
					}else{
		
						this.updateStatus(logs.toString(), "Failed");
		
					}

					break;
					
				}finally {
					
	//                session.saveHistory(logs.toString()); //write the failed record
					
				}
				
			}

			if("jupyter".equals(this.lang)){

				this.updateJupyterStatus(logs.toString(), "Done");

			}else{

				this.updateStatus(logs.toString(), "Done");

			}
			
						
			sendMessage2WebSocket("The process "+history_id+" is finished.");
			
			//this thread will end by itself when the task is finished, you don't have to close it manually

			GeoweaverController.sessionManager.closeByToken(token);

			
			
			log.info("Local session output thread ended");

		}catch(Exception e){

			e.printStackTrace();

			this.updateStatus(e.getLocalizedMessage(), "Failed");
		
		}finally{
			
			sendMessage2WebSocket("======= Process " + this.history_id + " ended");

			// cleanLogMonitor();
			

		}

    }
    

}
