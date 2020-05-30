package edu.gmu.csiss.earthcube.cyberconnector.ws;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.springframework.web.context.request.WebRequest;

/**
 * This works
 * @author JensenSun
 *
 */
//ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c
@ServerEndpoint(value = "/jupyter-socket/api/kernels/{uuid1}/channels")
public class JupyterServlet {
	
	Logger logger = Logger.getLogger(JupyterServlet.class);
	
	@OnOpen
    public void onOpen( final Session session) {
		
		logger.info("websocket channel openned");
		
		logger.info("Query String: " + session.getQueryString());
		
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) throws Throwable {
        
    	logger.error("websocket channel error" + throwable.getLocalizedMessage());
    	
    	throw throwable;
    	
    }

    @OnMessage
    public void onMessage(String message, @PathParam("uuid1") String uuid1, Session session) {
        
    	try {
    		
    		logger.info("Received message: " + message);
        	
        	logger.info("UUID string: " + uuid1 + " - Session ID: " + session.getQueryString());
        	
        	logger.info("Transfer message to Jupyter Notebook server..");
        	
        	logger.info("Receive message from Jupyter notebook");
        	
        	logger.info("Send the response message back to client");
        	
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    	
    }

    @OnClose
    public void onClose(final Session session) {
        
    	logger.error("Channel closed.");
    	
    }

}
