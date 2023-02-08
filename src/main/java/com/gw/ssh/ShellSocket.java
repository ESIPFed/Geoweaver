package com.gw.ssh;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import com.gw.web.GeoweaverController;

/**
 * Replaced by the TerminalServlet
 */
@Deprecated
public class ShellSocket implements WebSocketHandler {

    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "quit"});
    
    static Map<String, WebSocketSession> peers =new HashMap();


    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        
    	if (!(message instanceof TextMessage)) {
        
        	throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        
        }
        
        String messageText = ((TextMessage)message).getPayload(); //token from client
        
        SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
        
        if (sshSession == null) {
            
        	log.info("linking {}:{}", session.getId(), messageText);
            
            // TODO is there a better way to do this?
            // Can the client send the websocket session id and username in a REST call to link them up?
            sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(messageText);

            if(sshSession!=null) {
            	
            	sshSession.setWebSocketSession(session);
                
            	GeoweaverController.sessionManager.sshSessionByToken.put(session.getId(), sshSession);

                
            }else {
            	
            	session.sendMessage(new TextMessage("No SSH connection is active"));
            	
            	session.close();
            	
            }
            
        } else {
        	
            log.debug("message in {}:{}", session.getId(), messageText);
            
            sshSession.getSSHOutput().write((messageText + '\n').getBytes());
            
            sshSession.getSSHOutput().flush();
            
            // if we receive a valid logout command, then close the websocket session.
            // the system will log out and tidy itself up...
            
            if (logoutCommands.contains(messageText.trim().toLowerCase())) {
                
            	log.info("valid logout command received: {}", messageText);
            	
            	sshSession.logout();
            	
            	session.close(); //close WebSocket session. Notice: the SSHSession will continue to run.
            	
            }
            
        }
        
        
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.debug("websocket connection established");
        peers.put(session.getId(), session);
    }
    
    protected static WebSocketSession findSessionById(String sessionid) {
        if (peers.containsKey(sessionid)) {
            return peers.get(sessionid);
        }
        return null;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.debug("websocket connection closed: {}", status.getReason());
        //close SSH session
        if(GeoweaverController.sessionManager!=null) {
        	
        	SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
            if (sshSession != null && sshSession.isTerminal()) { //only close when it is shell
                sshSession.logout();
            }
            GeoweaverController.sessionManager.sshSessionByToken.remove(session.getId());
        	
        }
        peers.remove(session.getId());
        
    }


	@Override
	public void handleTransportError(WebSocketSession session,
			Throwable exception) throws Exception {
		log.warn(String.format("TRANSPORT ERROR: %s", exception.getMessage()));
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
