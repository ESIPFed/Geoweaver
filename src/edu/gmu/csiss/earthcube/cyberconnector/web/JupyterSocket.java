package edu.gmu.csiss.earthcube.cyberconnector.web;

import org.apache.log4j.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class JupyterSocket  implements WebSocketHandler {

	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		
		logger.info("After connection established");
		
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		
		logger.info("Received Socket Message");
		
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		
		logger.info("Handle Transportation Error");
		
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		
		logger.info("Socket Connection Closed");
		
	}

	@Override
	public boolean supportsPartialMessages() {
		
		logger.info("Supports Partial Messages");
		
		return false;
	}
	
	
	

}
