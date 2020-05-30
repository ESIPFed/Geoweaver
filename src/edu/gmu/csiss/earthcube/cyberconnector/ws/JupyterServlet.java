package edu.gmu.csiss.earthcube.cyberconnector.ws;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

/**
 * This works
 * @author JensenSun
 *
 */
@ServerEndpoint(value = "/jupyter-proxy")
public class JupyterServlet {
	
	Logger logger = Logger.getLogger(JupyterServlet.class);
	
	@OnOpen
    public void onOpen( final Session session) {
		
		logger.info("websocket channel openned");
		
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) {
        
    	logger.error("websocket channel error" + throwable.getLocalizedMessage());
    	
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        
    	logger.info("Received message: " + message);
    	
    	logger.info("Transfer message to Jupyter Notebook server..");
    	
    	logger.info("Receive message from Jupyter notebook");
    	
    	logger.info("Send the response message back to client");
    	
    }

    @OnClose
    public void onClose(final Session session) {
        
    	logger.error("Channel closed.");
    	
    }

}
