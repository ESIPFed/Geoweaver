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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;
import edu.gmu.csiss.earthcube.cyberconnector.web.GeoweaverController;

public class SSHSessionOutput implements Runnable {

    protected final Logger log = LoggerFactory.getLogger(getClass());

    private final BufferedReader in;
    
    private WebSocketSession out; // log&shell websocket
    
    private String token; // session token
    
    private boolean run = true;
    
    public SSHSessionOutput(BufferedReader in, String token) {
        log.info("created");
        this.in = in;
        this.token = token;
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
                
                System.out.println("thread output >> " + line);
                
                logs.append(line).append("\n");
                
                if(!BaseTool.isNull(out)) {
                	
                	if(prelog.toString()!=null) {
                		
                		line = prelog.toString() + line;
                		
                		prelog = new StringBuffer();
                		
                	}
                	
                    log.info("message out {}:{}", out.getId(), line);
                    
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
