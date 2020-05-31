package edu.gmu.csiss.earthcube.cyberconnector.ws;

import java.io.IOException;
import java.net.URI;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.apache.log4j.Logger;

import edu.gmu.csiss.earthcube.cyberconnector.utils.BaseTool;

@ClientEndpoint
public class Java2JupyterClientEndpoint {

	Session userSession = null;
    private Session servletsession;
    private Logger logger = Logger.getLogger(this.getClass());

    public Java2JupyterClientEndpoint(URI endpointURI, Session servletsession) {
        try {
        	this.servletsession = servletsession;
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, endpointURI);
            
//            WebSocketContainer container=ContainerProvider.getWebSocketContainer();
//            	container.connectToServer(this, new URI(uri));
        } catch (Exception e) {
        	e.printStackTrace();
            throw new RuntimeException(e);
        }
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
        this.userSession = userSession;
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
        	this.userSession = null;
        	logger.info("The connection between Javascript and Geoweaver is closed. ");
			if(!BaseTool.isNull(this.servletsession)) this.servletsession.close();
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
    	if(!BaseTool.isNull(this.servletsession)) this.servletsession.getAsyncRemote().sendText(message);
    	
    }

    /**
     * Send a message.
     * 
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
        this.userSession.getAsyncRemote().sendText(message);
    }

	
}
