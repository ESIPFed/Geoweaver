package edu.gmu.csiss.earthcube.cyberconnector.ws;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

@ClientEndpoint
public class Java2JupyterClientEndpoint extends Endpoint {

	Session newjupyteression = null;
    private Session jssession;
    private Logger logger = Logger.getLogger(this.getClass());

    public Java2JupyterClientEndpoint(URI endpointURI, Session jssession, Map<String, List<String>> headers) {
        try {
        	
        	this.jssession = jssession;
            
        	WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            
        	//build ClientEndpointConfig
            Builder configBuilder = ClientEndpointConfig.Builder.create();
            
//            configBuilder.configurator(new Configurator() {
//                @Override
//                public void beforeRequest(Map<String, List<String>> headers) {
//                	headers.put("Cookie", Arrays.asList("JSESSIONID=" + sessionID));
//                }
//            });
            ClientEndpointConfig clientConfig = configBuilder.build();
            
            Iterator<String> itr = headers.keySet().iterator();
            
            while (itr.hasNext())
            {
            	String key = itr.next();
            	
            	List<String> value = headers.get(key);

            	System.out.println(key + "=" + value);
            	
            	clientConfig.getUserProperties().put(key, value.get(0));
            	
            }
            
//            clientConfig.getConfigurator().beforeRequest(headers);
//            clientConfig.getUserProperties().put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
//            clientConfig.getUserProperties().put("Sec-WebSocket-Key", "JNOrKMA6YhDCRijp46/ofg==");
//            clientConfig.getUserProperties().put("Sec-WebSocket-Version", "13");
            
//            Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits
//            Sec-WebSocket-Key: JNOrKMA6YhDCRijp46/ofg==
//            Sec-WebSocket-Version: 13
            
            container.connectToServer(this, clientConfig, endpointURI);
            
//            Session newjupytersession = container.connectToServer(this,  endpointURI);
            
//            newjupytersession.getAsyncRemote().sendText("sdfds");
            
        } catch (Exception e) {
        	
        	e.printStackTrace();
            throw new RuntimeException(e);
            
        }
        
    }
    
    
    @Override
	public void onOpen(Session session, EndpointConfig config) {
    	logger.info("The connection between Java and Jupyter server is established.");
        this.newjupyteression = session;
	}
    
    /**
     * Callback hook for Connection open events.
     * 
     * @param userSession
     *            the userSession which is opened.
     */
    @OnOpen
    public void onOpen(Session userSession) {
    	logger.info("The connection between Java and Jupyter server is established.");
        this.newjupyteression = userSession;
    }

    /**
     * Callback hook for Connection close events.
     * 
     * @param userSession
     *            the userSession which is getting closed.
     * @param reason
     *            the reason for connection close
     */
    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        try {
        	logger.info("The connection between Java and Jupyter is closed.");
        	this.newjupyteression = null;
        	logger.info("The connection between Javascript and Geoweaver is closed. ");
			if(!BaseTool.isNull(this.jssession)) this.jssession.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    /**
     * Callback hook for Message Events. This method will be invoked when a
     * client send a message.
     * 
     * @param message
     *            The text message
     */
    @OnMessage
    public void onMessage(String message) {
    	logger.info("Received message from remote Jupyter server: " + message);
    	logger.info("send this message back to the client");
    	if(!BaseTool.isNull(this.jssession)) this.jssession.getAsyncRemote().sendText(message);
    	
    }

    /**
     * Send a message.
     * 
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
        this.newjupyteression.getAsyncRemote().sendText(message);
    }



	

	
}
