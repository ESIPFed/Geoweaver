package edu.gmu.csiss.earthcube.cyberconnector.ws;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

/**
 * This works
 * @author JensenSun
 *
 */
//ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c
@ServerEndpoint(value = "/jupyter-socket/api/kernels/{uuid1}/channels")
public class JupyterServlet {
	
	Logger logger = Logger.getLogger(JupyterServlet.class);
	
	Java2JupyterClientEndpoint client = null;
	
	@OnOpen
    public void onOpen(Session session, @PathParam("uuid1") String uuid1) {
		
		try {
			
			logger.info("websocket channel openned");
			
			String trueurl = "ws://localhost:8888/api/kernels/"+uuid1+
					"/channels?" + session.getQueryString();
			
			logger.info("Query String: " + trueurl);
			
			client = new Java2JupyterClientEndpoint(new URI(trueurl), session);
			
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
		
    }

    @OnError
    public void onError(final Session session, final Throwable throwable) throws Throwable {
        
	    	logger.error("websocket channel error" + throwable.getLocalizedMessage());
	    	
	    	throw throwable;
    	
    }

    @OnMessage
    public void onMessage(String message, @PathParam("uuid1") String uuid1, Session session) {
        
	    	try {
	    		
	    		if(BaseTool.isNull(client)) {
	    			
	    			session.close();
	    			
	    		}else {
	    			
	    			logger.info("Received message: " + message);
		        	
		        	logger.info("UUID string: " + uuid1 + " - Session ID: " + session.getQueryString());
		        	
		        	logger.info("Transfer message to Jupyter Notebook server..");
		        	
		        	client.sendMessage(message);
		        	
	    		}
	    		
	    	}catch(Exception e) {
	    		
	    		e.printStackTrace();
	    		
	    	}
    	
    	
    }

    @OnClose
    public void onClose(final Session session) {
    	
		try {
			
    		logger.error("Channel closed.");
        		
			client.newjupyteression.close(); //close websocket connection
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
    	
    }

}
