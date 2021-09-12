package com.gw.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;


import org.apache.tomcat.websocket.WsSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gw.ssh.SSHSession;
import com.gw.web.GeoweaverController;

/**
 * 
 * This class is used as the only websocket channel for transferring all the non-terminal SSH related message
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8080/geoweaver-shell-socket
@ServerEndpoint(value = "/command-socket")
public class CommandServlet {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * WebSocket Session between the client and Geoweaver
	 */
	private Session wsSession;
	
	private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "quit"});
    
    static Map<String, Session> peers = new HashMap();
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, EndpointConfig config) {
		
		try {
			
			logger.debug("Command-socket websocket channel openned");
			
			this.wsSession = session;
			
			WsSession wss = (WsSession) session;
			
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

            String tokenfromclient = null;

            // if(message!=null ){

            //     if(message.startsWith("history_id:")){
                    
            //         tokenfromclient = message.substring(11);

            //         logger.debug(" - History ID: " + tokenfromclient);

            //         WsSession wss = (WsSession) session;
                    
            //         // logger.debug("Web Socket Session ID:" + wss.getHttpSessionId());
                    
            //         peers.put(tokenfromclient, session);

            //     }else if(message.startsWith("token:")){

            //         tokenfromclient = message.substring(6);

            //         logger.debug(" - Token: " + tokenfromclient);

            //         WsSession wss = (WsSession) session;
                    
            //         logger.debug("Web Socket Session ID:" + wss.getHttpSessionId());
                    
            //         peers.put(tokenfromclient, session);

            //     }

				

            // }
        	
        	logger.debug(" Session ID: " + session.getQueryString());
        	
        	logger.debug("Transfer message to Jupyter Notebook server..");
        	
//        	session.getBasicRemote().sendText("Message received and Geoweaver Shell Socket Send back: " + message);
        	
            //the session should never be managed by their session id because the js session id could change after a while of stale
            SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
            
            if (sshSession == null) {
                
            	logger.debug("linking " + session.getId() + " - " + tokenfromclient);

                // TODO is there a better way to do this?
                // Can the client send the websocket session id and username in a REST call to link them up?
                sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(tokenfromclient);
                
//                if(sshSession!=null&&sshSession.getSSHInput().ready()) {
                if(sshSession!=null) {
                	
//                	sshSession.setWebSocketSession(session);
                    
                	GeoweaverController.sessionManager.sshSessionByToken.put(session.getId(), sshSession);
                	
//                	GeoweaverController.sessionManager.sshSessionByToken.remove(messageText); //remove session, a token can only be used once
                    
                }else {
                	
                	if(session.isOpen()) {
                		
                		session.getBasicRemote().sendText("No SSH connection is active");
                		
                	}
                	
//                	session.close();
                	
                }
                
            } else {
            	
                logger.debug("message in " + session.getId() + message);
                
                sshSession.getSSHOutput().write((message + '\n').getBytes());
                
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

    public void printoutCallStack(){

        System.out.println("Printing stack trace:");
        StackTraceElement[] elements = Thread.currentThread().getStackTrace();
        for (int i = 1; i < elements.length; i++) {
            StackTraceElement s = elements[i];
            System.out.println("\tnull websocket trace at " + s.getClassName() + "." + s.getMethodName() + "(" + s.getFileName() + ":" + s.getLineNumber() + ")");
        }
    }

    /**
     * Close session
     * @param session
     */
    @OnClose
    public void close(final Session session) {
    	
		try {

            printoutCallStack();
			
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
    
    /**
     * Find session by id
     * @param sessionid
     * @return
     */
    public static javax.websocket.Session findSessionById(String sessionid) {
    	javax.websocket.Session se = null;
        if (peers.containsKey(sessionid)) {
        	se = peers.get(sessionid);
        }
        return se;
    }

    public static void removeSessionById(String sessionid){

        peers.remove(sessionid);

    }

    public static void cleanAll(){

        peers.clear();

    }

}
