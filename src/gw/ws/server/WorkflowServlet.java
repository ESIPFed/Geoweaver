package gw.ws.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.EndpointConfig;
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

import gw.utils.BaseTool;

/**
 * 
 * This works but will be used for mointoring workflow
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c
@ServerEndpoint(value = "/workflow-socket")
public class WorkflowServlet {
	
	Logger logger = Logger.getLogger(WorkflowServlet.class);
	
	private Session wsSession;
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, EndpointConfig config) {
		
		try {
			
			logger.info("websocket channel openned");
			
			this.wsSession = session;
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
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
    		
			logger.info("Received message: " + message);
        	
        	logger.info(" - Session ID: " + session.getQueryString());
        	
        	logger.info("Transfer message to Jupyter Notebook server..");
        	
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }

    @OnClose
    public void close(final Session session) {
    	
		try {
			
    		logger.error("Channel closed.");
        	
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
    	
    }

}
