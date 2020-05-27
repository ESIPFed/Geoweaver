package edu.gmu.csiss.earthcube.cyberconnector.web;


import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.springframework.web.socket.TextMessage;

public class JupyterHandler extends TextWebSocketHandler{
	

	@Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) {
		
		// ...
		
		System.out.println("Handle Text Message");
		
	}


}
