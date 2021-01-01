package com.gw.server;

import javax.websocket.MessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WebSocket Message Handler
 * @author JensenSun
 * @date 12/31/2020
 */
public class WebsocketMessageHandler implements MessageHandler.Whole<String>{
	
	javax.websocket.Session jssession = null;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public WebsocketMessageHandler(Java2JupyterClientEndpoint client) {
		
		jssession = JupyterRedirectServlet.findPairBy2ndSession(client).getBrowse_geoweaver_session();
		
	}

	@Override
	public void onMessage(String message) {
		
		synchronized(jssession) {
			
    		try {
    			
//        		logger.debug("Received message from remote Jupyter server: " + message);
        	
//            	logger.debug("send this message back to the client");
            	
            	if(jssession!=null && jssession.isOpen()) {
            		
            		jssession.getBasicRemote().sendText(message);
            		
//            		logger.debug("the message should already be sent");
            		
            	}else {
            		
            		logger.warn("The websocket between browser and geoweaver is null or closed");
            		
            	}
            	
            	
//            	if(!bt.isNull(window)) {
//            		
//            		window.writeServerMessage(message);
//            		
//            	}
//                    session.getBasicRemote().sendText("Got message from " + session.getId() + "\n" + message);
    		} catch (Exception ex) {
    			ex.printStackTrace();
            	logger.error("Fail to parse the returned message from Jupyter server" + ex.getLocalizedMessage());
            	
            	
            }
    	}
		
	}
	
}
