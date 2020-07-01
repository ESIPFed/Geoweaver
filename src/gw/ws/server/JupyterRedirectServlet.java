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
import gw.ws.client.Java2JupyterClientEndpoint;

/**
 * 
 * This works for redirecting all the jupyter notebook traffic
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c
@ServerEndpoint(value = "/jupyter-socket/api/kernels/{uuid1}/channels", 
	configurator = JupyterRedirectServerConfig.class)
public class JupyterRedirectServlet {
	
	Logger logger = Logger.getLogger(JupyterRedirectServlet.class);
	
	Java2JupyterClientEndpoint client = null;
	
	private Session wsSession;
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, @PathParam("uuid1") String uuid1, EndpointConfig config) {
		
		try {
			
			logger.info("websocket channel openned");
			
			String trueurl = "ws://localhost:8888/api/kernels/"+uuid1+"/channels?" + session.getQueryString();
			
			logger.info("Query String: " + trueurl);
			
			this.wsSession = session;
			
//			this.httpSession = (HttpSession) config.getUserProperties()
//                    .get(HttpSession.class.getName());
			Map<String, List<String>> headers = (Map<String, List<String>>)config.getUserProperties().get("RequestHeaders");
			
			client = new Java2JupyterClientEndpoint(new URI(trueurl), session, headers);
			
		} catch (URISyntaxException e) {
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
    public void echo(String message, @PathParam("uuid1") String uuid1, Session session) {
        
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
    public void close(final Session session) {
    	
		try {
			
    		logger.error("Channel closed.");
        	
			client.getNewjupyteression().close(); //close websocket connection
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
    	
    }

}
