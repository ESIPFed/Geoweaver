package com.gw.server;

import javax.websocket.MessageHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gw.tools.JupyterSessionPairTool;
import com.gw.tools.SessionPair;

/**
 * WebSocket Message Handler
 * @author JensenSun
 * @date 12/31/2020
 */
public class WebsocketMessageHandler implements MessageHandler.Whole<String>{
	
	javax.websocket.Session jssession = null;
	
//	Java2JupyterClientEndpoint client;
	
	String pairid;
	
	Logger logger = LoggerFactory.getLogger(this.getClass());
	
	public WebsocketMessageHandler(String pairid) {
		
		this.pairid = pairid;
		
	}

	@Override
	public void onMessage(String message) {
			
		try {
			
//			logger.debug(pairid + "Message from Jupyter: " + message);
			
			SessionPair pair = JupyterSessionPairTool.findPairByID(pairid);
			
			if(pair==null) {
				
//				logger.debug(String.valueOf(JupyterRedirectServlet.pairs.size()));
				
//				throw new RuntimeException("The pair is null " + pairid);
				logger.debug("the pair is null " + pairid);
				
				return;
			}
				
			jssession = pair.getBrowse_geoweaver_session();
			
			synchronized(jssession) {
			
				
			
//	            	logger.debug("send this message back to the client");
				
				if(jssession!=null && jssession.isOpen() && message!=null) {
					
					jssession.getBasicRemote().sendText(message);
					
//	            		logger.debug(pair.getId() + " transferred to browser");
					
				}else {
					
					logger.warn(pair.getId() + "The websocket between browser and geoweaver is null or closed");
					
				}
				
				
//	            	if(!BaseTool.isNull(window)) {
//	            		
//	            		window.writeServerMessage(message);
//	            		
//	            	}
//	              session.getBasicRemote().sendText("Got message from " + session.getId() + "\n" + message);
			

			}
			
			
		} catch (Exception ex) {
			
			ex.printStackTrace();
        	
			logger.error("Fail to parse the returned message from Jupyter server" + ex.getLocalizedMessage());
        	
        	
        }
		
	}
	
}
