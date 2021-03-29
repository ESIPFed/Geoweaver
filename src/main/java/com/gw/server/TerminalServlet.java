package com.gw.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;


import org.apache.tomcat.websocket.WsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.web.GeoweaverController;

/**
 * 
 * This is used as the only websocket channel for transferring all the terminal SSH related message
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8080/geoweaver-shell-socket
@ServerEndpoint(value = "/terminal-socket")
public class TerminalServlet {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	private Session wsSession;
	
	private String httpSessionID;
	
	private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "quit"});
    
    static Map<String, Session> peers =new HashMap();
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, EndpointConfig config) {
		
		try {
			
			logger.debug("websocket channel openned");
			
			this.wsSession = session;
			
			WsSession wss = (WsSession) session;
			
			httpSessionID = wss.getHttpSessionId();
			
			logger.debug("Web Socket Session ID:" + wss.getHttpSessionId());
			
			peers.put(wss.getHttpSessionId(), session);
			
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
		
    }

    @OnError
    public void error(final Session session, final Throwable throwable) throws Throwable {
        
    	logger.error("websocket channel error" + throwable.getLocalizedMessage());
    	
    	throw throwable;
    	
    }

    @OnMessage
    public void echo(String message, Session session) {
        
    	try {
    		
			logger.debug("Received message: " + message);
        	
//        	session.getBasicRemote().sendText("Message received and Geoweaver Shell Socket Send back: " + message);
        	
//            SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
			
			SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(httpSessionID);
            
            if (sshSession == null) {
                
            	logger.debug("linking " + session.getId() + message);
                
                // TODO is there a better way to do this?
                // Can the client send the websocket session id and username in a REST call to link them up?
                sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(message);
                
//                if(sshSession!=null&&sshSession.getSSHInput().ready()) {
                if(sshSession!=null) {
                	
//                	sshSession.setWebSocketSession(session);
                    
                	GeoweaverController.sessionManager.sshSessionByToken.put(session.getId(), sshSession);
                	
//                	GeoweaverController.sessionManager.sshSessionByToken.remove(messageText); //remove session, a token can only be used once
                    
                }else {
                	
                	session.getBasicRemote().sendText("No SSH connection is active");
                	
//                	session.close();
                	
                }
                
            } else {
            	
                logger.debug("message in " + session.getId() + message);
                
                sshSession.getSSHOutput().write((message).getBytes()); 
                
                
                sshSession.getSSHOutput().flush();
                
//    			//send Ctrl + C command to the SSH to close the connection
//    			
//    			cmd.getOutputStream().write(3);
//    			
//    		    cmd.getOutputStream().flush();
                
                // if we receive a valid logout command, then close the websocket session.
                // the system will logout and tidy itself up...
                
                if (logoutCommands.contains(message.trim().toLowerCase())) {
                    
                	logger.debug("valid logout command received " +  message);
                	
                	sshSession.logout();
                	
//                	session.close(); //close WebSocket session. Notice: the SSHSession will continue to run.
                	
                }
                
            }
            
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }

    @OnClose
    public void close(final Session session) {
    	
		try {
			
    		logger.debug("Geoweaver Shell Channel closed.");
    		
    		logger.debug("websocket session closed:" + session.getId());
    		
            //close SSH session
            if(GeoweaverController.sessionManager!=null) {
            	
            	SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
                if (sshSession != null && sshSession.isTerminal()) { //only close when it is shell
                    sshSession.logout();
                }
                GeoweaverController.sessionManager.sshSessionByToken.remove(session.getId());
            	
            }
            peers.remove(session.getId());
        	
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
    	
    }
    
    
    public static javax.websocket.Session findSessionById(String sessionid) {
        if (peers.containsKey(sessionid)) {
            return peers.get(sessionid);
        }
        return null;
    }

}
