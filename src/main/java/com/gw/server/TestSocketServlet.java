package com.gw.server;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


//ws://geoweaver2.eastus.cloudapp.azure.com:8000/user/zsun/api/kernels/eaa5f686-0df2-4f39-ae95-d6715d9f7fc5/channels?session_id=f7550de984274d3785f246b3e9eb1c0a
@ServerEndpoint(value = "/xyz-socket")
public class TestSocketServlet {
	
//	public TestSocketServlet() {
//		super();
//		
//		System.out.println("Test websocket servlet is created!");
//		
//		
//	}
	
	
//	@OnOpen
//    public void open(Session session, 
////    		@PathParam("hostid") String hostid, 
////    		@PathParam("uname") String username, 
////    		@PathParam("uuid1") String uuid1, 
//    		EndpointConfig config) {
//		
//		
//    }
//
//    @OnError
//    public void error(final Session session, final Throwable throwable) throws Throwable {
//    	
//    	
//    }
//
//    @OnMessage(maxMessageSize = 10000000)
//    public void echo(String message, 
////    		@PathParam("uuid1") String uuid1, 
//    		Session session) {
//    	
//    	System.out.println("onMessage::From=" + session.getId() + " Message=" + message);
//        
//        try {
//            session.getBasicRemote().sendText("Hello Client " + session.getId() + "!");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    	
//    }
//
//    @OnClose
//    public void close(final Session session) {
//    	
//    	
//    }
	

	Logger logger = LoggerFactory.getLogger(getClass());
	
	/**
	 * WebSocket Session between the client and Geoweaver
	 */
	private Session wsSession;
	
	private List<String> logoutCommands = Arrays.asList(new String[]{"logout", "quit"});
    
    static Map<String, Session> peers = new HashMap();
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, EndpointConfig config) {
		
		try {
			
			logger.debug("websocket channel openned");
			
//			this.wsSession = session;
//			
//			WsSession wss = (WsSession) session;
			
//			logger.debug("Web Socket Session ID:" + wss.getHttpSessionId());
			
//			peers.put(wss.getHttpSessionId(), session);
			
		} catch (Exception e) {
			
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
    		
			logger.debug("Received message: " + message);
        	
        	logger.debug(" Session ID: " + session.getQueryString());
        	
        	logger.debug("Transfer message to Jupyter Notebook server..");
        	
//        	session.getBasicRemote().sendText("Message received and Geoweaver Shell Socket Send back: " + message);
        	
//            SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
//            
//            if (sshSession == null) {
//                
//            	logger.debug("linking " + session.getId() + message);
//                
//                // TODO is there a better way to do this?
//                // Can the client send the websocket session id and username in a REST call to link them up?
//                sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(message);
//                
////                if(sshSession!=null&&sshSession.getSSHInput().ready()) {
//                if(sshSession!=null) {
//                	
////                	sshSession.setWebSocketSession(session);
//                    
//                	GeoweaverController.sessionManager.sshSessionByToken.put(session.getId(), sshSession);
//                	
////                	GeoweaverController.sessionManager.sshSessionByToken.remove(messageText); //remove session, a token can only be used once
//                    
//                }else {
//                	
//                	if(session.isOpen()) {
//                		
//                		session.getAsyncRemote().sendText("No SSH connection is active");
//                		
//                	}
//                	
////                	session.close();
//                	
//                }
//                
//            } else {
//            	
//                logger.debug("message in " + session.getId() + message);
//                
//                sshSession.getSSHOutput().write((message + '\n').getBytes());
//                
//                sshSession.getSSHOutput().flush();
//                
////    			//send Ctrl + C command to the SSH to close the connection
////    			
////    			cmd.getOutputStream().write(3);
////    			
////    		    cmd.getOutputStream().flush();
//                
//                // if we receive a valid logout command, then close the websocket session.
//                // the system will logout and tidy itself up...
//                
//                if (logoutCommands.contains(message.trim().toLowerCase())) {
//                    
//                	logger.debug("valid logout command received " +  message);
//                	
//                	sshSession.logout();
//                	
////                	session.close(); //close WebSocket session. Notice: the SSHSession will continue to run.
//                	
//                }
//                
//            }
            
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }

    @OnClose
    public void close(final Session session) {
    	
		try {
			
    		logger.debug("Geoweaver Shell Channel closed.");
    		
    		logger.debug("websocket session closed:" + session.getId());
    		
            //close SSH session
//            if(GeoweaverController.sessionManager!=null) {
//            	
//            	SSHSession sshSession = GeoweaverController.sessionManager.sshSessionByToken.get(session.getId());
//                if (sshSession != null && sshSession.isTerminal()) { //only close when it is shell
//                    sshSession.logout();
//                }
//                GeoweaverController.sessionManager.sshSessionByToken.remove(session.getId());
//            	
//            }
//            peers.remove(session.getId());
        	
		} catch (Exception e) {
			
			e.printStackTrace();
			
		}
    	
    }
    
    

}
