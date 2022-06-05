package com.gw.server;

import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.websocket.ClientEndpointConfig;
import javax.websocket.ClientEndpointConfig.Builder;
import javax.websocket.ClientEndpointConfig.Configurator;
import javax.websocket.CloseReason;
import javax.websocket.ContainerProvider;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.gw.jpa.Host;
import com.gw.tools.JupyterSessionPairTool;
import com.gw.tools.SessionPair;
import com.gw.utils.BaseTool;

/**
 * 
 * @author JensenSun
 *
 */
//@ClientEndpoint
@Service
@Scope("prototype")
public class Java2JupyterClientEndpoint extends Endpoint 
{

	Session new_ws_session_between_geoweaver_and_jupyterserver = null;
//	
	Session new_ws_session_between_browser_and_geoweaver = null;
	
    private Logger logger = LoggerFactory.getLogger(getClass());
    
    String pairid;
    
    @Autowired
    BaseTool bt;
    

    public static String upperAllFirst(String key) {
    	
    	String[] ks = key.split("-");
    	
    	for(int i=0;i<ks.length;i++) {
    		
    		ks[i] = ks[i].substring(0, 1).toUpperCase() + ks[i].substring(1);
    		
    		ks[i] = ks[i].replace("Websocket", "WebSocket");
    		
    	}
    	
    	StringBuffer sb = new StringBuffer();
    	
    	for(int i=0;i<ks.length;i++) {
    		
    		if(i!=0)sb.append("-");
    		
    		sb.append(ks[i]);
    		
    	}
    	
    	return sb.toString();
    	
    }
    
    public Java2JupyterClientEndpoint() {
    	
    	logger.warn("The class is not initiated correctly.");
    	
    }
    
    
    public void init(URI endpointURI, Session jssession, Map<String, List<String>> headers, Host h, String pairid) {
    	
        try {
        	
        	this.new_ws_session_between_browser_and_geoweaver = jssession;
        	
        	this.pairid = pairid;
        	
        	WebSocketContainer container = ContainerProvider.getWebSocketContainer();
        	
        	//build ClientEndpointConfig
            Builder configBuilder = ClientEndpointConfig.Builder.create();
            
            configBuilder.configurator(new Configurator() {
            	
            	@Override
            	public void afterResponse(HandshakeResponse hr) {
            		
            		logger.debug("Response Headers from Jupyter : " + hr.getHeaders());

                }
            	
                @Override
                public void beforeRequest(Map<String, List<String>> nativeheaders) {
//                	headers.put("Cookie", Arrays.asList("JSESSIONID=" + sessionID));
                	
                	logger.debug("Original Native Headers: " + nativeheaders);
                	
                	Map<String, List<String>> uppercaseheaders = new HashMap();
                	
                	Iterator hmIterator = headers.entrySet().iterator(); 
                	
                	String[] hostjupyters = bt.parseJupyterURL(h.getUrl());
                	
                    while (hmIterator.hasNext()) {
                        Map.Entry<String, List<String>> mapElement = (Map.Entry)hmIterator.next(); 
                        
                        String newkey = mapElement.getKey();
                        
                        newkey = upperAllFirst(newkey);
//                        newkey = newkey.toLowerCase();
                        
                        List<String> values = mapElement.getValue();
                        
//                        if("Sec-WebSocket-Key".equals(newkey)) {
                    	// if("Host".equals(newkey) || "Origin".equals(newkey) ) {
						if("Host".equals(newkey) || "Origin".equals(newkey) || "Sec-WebSocket-Key".equals(newkey)) {
                        	
                        	continue;
                        	
                        }
                        
//                        if("Host".equals(newkey) ) {
//                        	
//                        	List<String> local = new ArrayList();
//                        	local.add(hostjupyters[1] + ":" + hostjupyters[2]);
//                        	uppercaseheaders.put(newkey, local);
//                        	
//                        }else if ("Origin".equals(newkey)){
//                        	
//                        	List<String> local = new ArrayList();
////                        	local.add("http://localhost:8888");
//                        	local.add(hostjupyters[0] + "://" + hostjupyters[1] + ":" + hostjupyters[2]);
//                        	uppercaseheaders.put(newkey, local);
//                        	
//                        }else {
                        	uppercaseheaders.put(newkey, values);
                        	
//                        	logger.debug("Key:" + newkey + " - Values: " + values);
//                        }
                        
                    } 
                    
                	nativeheaders.putAll(uppercaseheaders);
//                    nativeheaders = uppercaseheaders;
                	
                	logger.debug("New Native Headers Loggout: " + nativeheaders);
                	
                }
                
            });
            
            
            
            
            ClientEndpointConfig clientConfig = configBuilder.build();
            
//            if(!BaseTool.isNull(headers)) {
//            	
//            	Iterator<String> itr = headers.keySet().iterator();
//                
//                while (itr.hasNext())
//                {
//                	String key = itr.next();
//                	
//                	List<String> value = headers.get(key);
//
//                	System.out.println(key + "=" + value);
//                	
//                	clientConfig.getUserProperties().put(key, value.get(0));
//                	
//                }
//            	
//            }
            
//            ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
//                public void beforeRequest(Map> headers) {
//                    headers.put("Authorization", asList("Basic " + DatatypeConverter.printBase64Binary("user:password".getBytes())));
//                }
//            };
//            ClientEndpointConfig clientConfig = ClientEndpointConfig.Builder.create()
//                    .configurator(configurator)
//                    .build();
            
//            clientConfig.getConfigurator().beforeRequest(headers);
//            clientConfig.getUserProperties().put("Sec-WebSocket-Extensions", "permessage-deflate; client_max_window_bits");
//            clientConfig.getUserProperties().put("Sec-WebSocket-Key", "JNOrKMA6YhDCRijp46/ofg==");
//            clientConfig.getUserProperties().put("Sec-WebSocket-Version", "13");
            
//            Sec-WebSocket-Extensions: permessage-deflate; client_max_window_bits
//            Sec-WebSocket-Key: JNOrKMA6YhDCRijp46/ofg==
//            Sec-WebSocket-Version: 13
            
            logger.debug("Default Binary Max Message Buffer Size: " + container.getDefaultMaxBinaryMessageBufferSize());
            
            container.setDefaultMaxBinaryMessageBufferSize(10000000); //limit the binary size to 10MB
            
            container.setDefaultMaxTextMessageBufferSize(10000000); //limit the text size to 100MB
            
            container.connectToServer(this, clientConfig, endpointURI);
            
            logger.debug("The connection to Jupyter server is built");
//            container.connectToServer(this, endpointURI);
            
//            Session newjupytersession = container.connectToServer(this,  endpointURI);
            
//            newjupytersession.getAsyncRemote().sendText("sdfds");
            
        } catch (Exception e) {
        	
        	e.printStackTrace();
            throw new RuntimeException(e);
            
        }
        
    }
    
    @Override
	public void onOpen(Session session, EndpointConfig config) {
    	
    	logger.debug("Override The connection between Java and Jupyter server is established.");
    	
        this.new_ws_session_between_geoweaver_and_jupyterserver = session;
    	
    	session.addMessageHandler(new WebsocketMessageHandler(this.pairid));
        
//        session.addMessageHandler(new MessageHandler.Whole<String>() {
//            @Override
//            public void onMessage(String message) {
//            		
//                	synchronized(jssession) {
//                		try {
//                			
////	                		logger.debug("Received message from remote Jupyter server: " + message);
//	                	
////		                	logger.debug("send this message back to the client");
//		                	
//		                	if(!BaseTool.isNull(jssession) && jssession.isOpen()) {
//		                		
//		                		jssession.getBasicRemote().sendText(message);
//		                		
////		                		logger.debug("the message should already be sent");
//		                		
//		                	}else {
//		                		
//		                		logger.warn("The websocket between browser and geoweaver is null or closed");
//		                		
//		                	}
//		                	
//		                	
////		                	if(!BaseTool.isNull(window)) {
////		                		
////		                		window.writeServerMessage(message);
////		                		
////		                	}
//		//                    session.getBasicRemote().sendText("Got message from " + session.getId() + "\n" + message);
//                		} catch (Exception ex) {
//                			ex.printStackTrace();
//                        	logger.error("Fail to parse the returned message from Jupyter server" + ex.getLocalizedMessage());
//                        	
//                        	
//                        }
//                	}
//                
//            }
//        });
    	
	}
    
    @Override
    public void onClose(Session session, CloseReason closeReason) {
		try {
			
	        logger.debug("Peer " + session.getId() + " disconnected due to " + closeReason.getReasonPhrase());
//	        this.new_ws_session_between_geoweaver_and_jupyterserver = null;
//	    	logger.debug("The connection between Javascript and Geoweaver is closed. ");
	    	
	    	SessionPair pair = JupyterSessionPairTool.findPairByID(this.pairid);
	    	
	    	if(!BaseTool.isNull(pair)) {
	    		
				pair.getBrowse_geoweaver_session().close();
				pair.getGeoweaver_jupyter_client().getNew_ws_session_between_geoweaver_and_jupyterserver().close();
	    		
	    	}
	    	
//	    	JupyterRedirectServlet.removeClosedPair();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    
    
    @Override
    public void onError(Session session, Throwable error) {
        logger.error("Error communicating with peer " + session.getId() + ". Detail: "+ error.getMessage());
//        JupyterRedirectServlet.removeClosedPair();
    }
    
    /**
     * Send a message.
     * 
     * @param user
     * @param message
     */
    public void sendMessage(String message) {
    	
//    	if(!BaseTool.isNull(this.new_ws_session_between_geoweaver_and_jupyterserver)) {
//    	SessionPair pair = JupyterRedirectServlet.findPairBy2ndSession(this);
    	
    	if(!BaseTool.isNull(this.new_ws_session_between_geoweaver_and_jupyterserver)) {
    		
        	synchronized(new_ws_session_between_geoweaver_and_jupyterserver) {
            	
            	 try {
//            		 logger.debug("pass message to jupyter " + this.pairid);
            		 new_ws_session_between_geoweaver_and_jupyterserver.getBasicRemote().sendText(message);
    			} catch (IOException e) {
    				e.printStackTrace();
//    				this.sendMessage(e.getMessage());
    			}
            	
            }
    	}
    		
//    	}
        
    }
    
    
	public Session getNew_ws_session_between_geoweaver_and_jupyterserver() {
		return new_ws_session_between_geoweaver_and_jupyterserver;
	}

	public void setNew_ws_session_between_geoweaver_and_jupyterserver(
			Session new_ws_session_between_geoweaver_and_jupyterserver) {
		this.new_ws_session_between_geoweaver_and_jupyterserver = new_ws_session_between_geoweaver_and_jupyterserver;
	}
//
//	public Session getNew_ws_session_between_browser_and_geoweaver() {
//		return new_ws_session_between_browser_and_geoweaver;
//	}
//
//	public void setNew_ws_session_between_browser_and_geoweaver(Session new_ws_session_between_browser_and_geoweaver) {
//		this.new_ws_session_between_browser_and_geoweaver = new_ws_session_between_browser_and_geoweaver;
//	}

//	public Java2JupyterClientDialog getWindow() {
//		return window;
//	}
//	
//	public void setWindow(Java2JupyterClientDialog window) {
//		this.window = window;
//	}
	
//	/**
//     * Callback hook for Connection open events.
//     * 
//     * @param userSession
//     *            the userSession which is opened.
//     */
//    @OnOpen
//    public void onOpen(Session userSession) {
//    	logger.info("Annotation The connection between Java and Jupyter server is established.");
//        this.newjupyteression = userSession;
//    }
	
//    /**
//     * Callback hook for Connection close events.
//     * 
//     * @param userSession
//     *            the userSession which is getting closed.
//     * @param reason
//     *            the reason for connection close
//     */
//    @OnClose
//    public void onClose(Session userSession, CloseReason reason) {
//        try {
//        	logger.info("The connection between Java and Jupyter is closed.");
//        	this.newjupyteression = null;
//        	logger.info("The connection between Javascript and Geoweaver is closed. ");
//			if(!BaseTool.isNull(this.newjupyteression)) this.newjupyteression.close();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//    }
//
//    /**
//     * Callback hook for Message Events. This method will be invoked when a
//     * client send a message.
//     * 
//     * @param message
//     *            The text message
//     */
//    @OnMessage
//    public void onMessage(String message) {
//    	logger.info("Received message from remote Jupyter server: " + message);
//    	logger.info("send this message back to the client");
////    	if(!BaseTool.isNull(this.newjupyteression)) this.newjupyteression.getAsyncRemote().sendText(message);
//    	if(!BaseTool.isNull(window)) {
//    		
//    		window.writeServerMessage(message);
//    		
//    	}
//    	
//    }
	
	
}
