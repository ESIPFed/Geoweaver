package com.gw.ssh;
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

import javax.websocket.Session;

import  com.gw.server.TerminalServlet;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
/**
 * 
 * This class is used for monitoring the traffic from SSH session/Terminal console
 * 
 * @author JensenSun
 *
 */
@Service
@Scope("prototype")
public class SSHLiveSessionOutput implements Runnable {

    protected Logger log = LoggerFactory.getLogger(getClass());

    protected BufferedReader in;
    
    protected WebSocketSession out; // log&shell websocket - not used any more
    
    protected Session wsout;
    
    protected String token; // session token
    
    protected boolean run = true;
    
    protected String history_id;
    
    @Autowired
    BaseTool bt;
    
    public SSHLiveSessionOutput() {
    	
    	//for spring
    	
    }
    
    public void init(BufferedReader in, String token) {
        log.info("created");
        this.in = in;
        this.token = token;
        run = true;
        wsout = TerminalServlet.findSessionById(token);
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

        String line = null;

    	try{

            while ((line = in.readLine())!=null) {
        	
                try {
                    
                    // readLine will block if nothing to send
                    
                    // String line = in.readLine();
                    
                    log.debug("shell thread output >> " + line);
                    
                    if(!BaseTool.isNull(wsout) && wsout.isOpen()) {
                        
                        log.debug("wsout message {}:{}", token, line);

                        wsout.getBasicRemote().sendText(line); // for the All information web socket session
                        
                    }else {
                        
                        wsout = TerminalServlet.findSessionById(token);
                        
                    }
                    
                } catch (Exception e) {
                    
                    e.printStackTrace();
                    
                    // GeoweaverController.sessionManager.closeByToken(token);
                    
                }
                
            }
            
            log.debug("SSH session output thread ended");

        }catch(Exception e){

            e.printStackTrace();
        }
        

    }
    
    public void setWebSocketSession(WebSocketSession session) {
        log.info("received websocket session");
//        this.out = session;
    }
    
}
