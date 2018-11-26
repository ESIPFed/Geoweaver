package edu.gmu.csiss.earthcube.cyberconnector.tasks;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class TaskSocket  implements WebSocketHandler {

	protected final Logger log = LoggerFactory.getLogger(getClass());
	
	private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "exit"});
	
	@Override
	public void afterConnectionClosed(WebSocketSession arg0, CloseStatus arg1) throws Exception {
		
		
		
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession arg0) throws Exception {
		
		
		
	}

	@Override
	public void handleMessage(WebSocketSession socketsession, WebSocketMessage<?> message) throws Exception {

		if (!(message instanceof TextMessage)) {
	        
        	throw new IllegalStateException("Unexpected WebSocket message type: " + message);
        
        }
        
        String messageText = ((TextMessage)message).getPayload(); //historyid from client
        
//        String[] st = messageText.split("="); // sessionid = taskname
        
        TaskManager.monitorTask(messageText, socketsession);
        
        if (logoutCommands.contains(messageText.trim().toLowerCase())) {
            
        	log.info("valid logout command received: {}", messageText);
        	
        	socketsession.close(); //close WebSocket session. Notice: the SSHSession will continue to run.
        	
        }
		
	}

	@Override
	public void handleTransportError(WebSocketSession arg0, Throwable exception) throws Exception {
		
		log.warn(String.format("TRANSPORT ERROR: %s", exception.getMessage()));
		
	}

	@Override
	public boolean supportsPartialMessages() {
		
		return false;
		
	}

	
	
}
