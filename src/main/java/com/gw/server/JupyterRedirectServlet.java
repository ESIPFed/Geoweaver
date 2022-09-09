package com.gw.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gw.jpa.Host;
import com.gw.tools.HostTool;
import com.gw.tools.JupyterSessionPairTool;
import com.gw.tools.SessionPair;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;

/**
 * 
 * This works for redirecting all the jupyter notebook traffic
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8080/Geoweaver/jupyter-socket/api/kernels/884447f1-bac6-4913-be86-99da11b2a78a/channels?session_id=42b8261488884e869213604975141d8c

@ServerEndpoint(value = "/jupyter-socket/{hostid}/api/kernels/{uuid1}/channels", 
	configurator = JupyterRedirectServerConfig.class)
public class JupyterRedirectServlet {
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
//	Session wsSession;
	
//	@Autowired
//	Java2JupyterClientEndpoint client;
	
	// public static List<SessionPair> pairs = new ArrayList();
	
	// @Autowired
	HostTool ht;
	
	// @Autowired
	BaseTool bt;
	
	public JupyterRedirectServlet() {
		
		logger.debug("Initializing Jupyter Websocket Session...");
		
	}
	
	// /**
	//  * First session is between browser and geoweaver
	//  * @param b2gsession
	//  * @return
	//  */
	// public static SessionPair findPairBy1stSession(Session b2gsession) {
		
	// 	SessionPair pair = null;
		
	// 	for(SessionPair p : pairs) {
			
	// 		if(p.getBrowse_geoweaver_session()==b2gsession) {
				
	// 			pair = p;
				
	// 			break;
				
	// 		}
			
	// 	}
		
	// 	return pair;
		
	// }
	
	// public static SessionPair findPairByID(String pairid) {
		
	// 	SessionPair pair = null;
		
	// 	for(SessionPair p : pairs) {
			
	// 		if(p.getId().equals(pairid)) {
				
	// 			pair = p;
				
	// 			break;
				
	// 		}
			
	// 	}
		
	// 	return pair;
		
	// }
	
	// /**
	//  * 2nd session is between geoweaver and jupyter
	//  * @param b2gsession
	//  * @return
	//  */
	// public static SessionPair findPairBy2ndSession(Java2JupyterClientEndpoint g2jclient) {
		
	// 	SessionPair pair = null;
		
	// 	for(SessionPair p : pairs) {
			
	// 		if(p.getGeoweaver_jupyter_client()==g2jclient) {
				
	// 			pair = p;
				
	// 			break;
				
	// 		}
			
	// 	}
		
	// 	return pair;
		
	// }
	
	// public static void removeClosedPair() {
		
	// 	for(SessionPair p : pairs) {
			
	// 		if(!p.getBrowse_geoweaver_session().isOpen() ) {
				
	// 			System.out.println("Detected one browser_geoweaver_session is closed. Removing it...");
				
	// 			pairs.remove(p);
				
	// 		}
			
	// 		if(!p.getGeoweaver_jupyter_client().getNew_ws_session_between_geoweaver_and_jupyterserver().isOpen()) {
				
	// 			System.out.println("Detected one geoweaver jupyter session is closed. Removing it...");
				
	// 			pairs.remove(p);
				
	// 		}
			
	// 	}
		
	// }
	
	// public static void removePairByID(String id) {
		
	// 	for(SessionPair p : pairs) {
			
	// 		if(p.getId().equals(id)) {
				
	// 			System.out.println("Detected one session pair is closed on one of the websocket session. Removing it...");
				
	// 			pairs.remove(p);
				
	// 			break;
				
	// 		}
			
	// 	}
		
	// }
	
	private void init(Session b2gsession) {
		
		if(ht==null) {
			
			ht = BeanTool.getBean(HostTool.class);
			
		}
		
		if(bt==null) {
			
			bt = BeanTool.getBean(BaseTool.class);
			
		}	
		
//		SessionPair pair = findPairBy1stSession(b2gsession);
		SessionPair pair = JupyterSessionPairTool.findPairByID(b2gsession.getQueryString());
		
		if(BaseTool.isNull(pair)) {
			
			Java2JupyterClientEndpoint client = BeanTool.getBean(Java2JupyterClientEndpoint.class);
			
			pair = new SessionPair();
			
			pair.setId(b2gsession.getQueryString());
			
			pair.setBrowse_geoweaver_session(b2gsession);
			
			pair.setGeoweaver_jupyter_client(client);
			
			JupyterSessionPairTool.pairs.add(pair);
			
			logger.debug("New Pair is created: ID: " + pair.getId());
			
		}else {
			
			logger.debug("A pair is found, no need to create new one: " + pair.getId());
			
		}
		
//		if(client==null) {
//			
//			Java2JupyterClientEndpoint client = BeanTool.getBean(Java2JupyterClientEndpoint.class);
//			
//		}
		
		
		
	}
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, @PathParam("hostid") String hostid, @PathParam("uuid1") String uuid1, EndpointConfig config) {
		
		try {
			
			init(session);
			
			logger.debug("websocket channel to host "+ hostid +" openned");
			
			Host h = ht.getHostById(hostid);
			
			String[] hh = bt.parseJupyterURL(h.getUrl());
			
			String wsprotocol = "ws";
			
			String trueurl = wsprotocol + "://"+hh[1]+":"+hh[2]+"/api/kernels/"+uuid1+"/channels?" + session.getQueryString();
			
//			logger.debug("Query String: " + trueurl);
			
//			this.wsSession = session;
			
//			this.httpSession = (HttpSession) config.getUserProperties()
//                    .get(HttpSession.class.getName());
			Map<String, List<String>> headers = (Map<String, List<String>>)config.getUserProperties().get("RequestHeaders");
			
//			client = new Java2JupyterClientEndpoint(new URI(trueurl), session, headers, h);
			
			SessionPair pair = JupyterSessionPairTool.findPairByID(session.getQueryString());
//			
			pair.getGeoweaver_jupyter_client().init(new URI(trueurl), session, headers, h, pair.getId());
			
//			client.init(new URI(trueurl), session, headers, h);
			
//			logger.debug("The connections from javascript end to this servlet, and this servlet to Jupyter server have been created.");
			
		} catch (URISyntaxException e) {
			
			e.printStackTrace();
			
		}
		
    }

    @OnError
    public void error(final Session session, final Throwable throwable) throws Throwable {
    	
//    	removeClosedPair();
    	
    	logger.error("websocket channel error" + throwable.getLocalizedMessage());
    	
    	throw throwable;
    	
    }

    @OnMessage(maxMessageSize = 10000000)
    public void echo(String message, @PathParam("uuid1") String uuid1, Session session) {
        
    	try {
    		
//    		init();
    		
//    		SessionPair pair = findPairBy1stSession(session);
    		
    		SessionPair pair = JupyterSessionPairTool.findPairByID(session.getQueryString());
    		
    		if(BaseTool.isNull(pair)) {
    			
    			logger.error("Cann't find the corresponding session pair");
    			
//    			session.close();
    			
    		}else {
    			
//    			logger.debug(pair.getId() + " Message from Browser: " + message);
//	        	
//	        	logger.debug("UUID string: " + uuid1 + " - Session ID: " + session.getQueryString());
	        	
//	        	logger.debug("Transfer message to Jupyter Notebook server..");
	        	
	        	pair.getGeoweaver_jupyter_client().sendMessage(message);
//	        	client.sendMessage(message);
	        	
    		}
    		
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }

    @OnClose
    public void close(final Session session) {
    	
		try {
			
    		logger.info("Channel closed.");
    		
    		SessionPair pair = JupyterSessionPairTool.findPairByID(session.getQueryString());
        	
    		if(!BaseTool.isNull(pair)) {
    			
    			pair.getGeoweaver_jupyter_client().getNew_ws_session_between_geoweaver_and_jupyterserver().close(); //close websocket connection
        		
        		JupyterSessionPairTool.pairs.remove(pair);
    			
//    			client.getNew_ws_session_between_geoweaver_and_jupyterserver().close();
    			
    		}
    		
//    		removeClosedPair();
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
    	
    }
    
    
    
    
//     public class SessionPair{
    	
//     	String id;
    	
//     	Session browse_geoweaver_session;
    	
//     	Java2JupyterClientEndpoint geoweaver_jupyter_client;
    	
// 		public String getId() {
// 			return id;
// 		}

// 		public void setId(String id) {
// 			this.id = id;
// 		}
		
// 		public Session findOpenSession() {
			
// 			Set<Session> sessionset = browse_geoweaver_session.getOpenSessions();
			
// 			Iterator it = sessionset.iterator();
			
// 			Session session = browse_geoweaver_session;
			
// 		     while(it.hasNext()){
// //		        System.out.println(it.next());
		    	 
// 		    	 Session cs = (Session)it.next();
		    	 
// 		    	 if(id.equals(cs.getQueryString())) {
		    		 
// 		    		 session = cs;
		    		 
// 		    		 break;
		    		 
// 		    	 }
		    	 
// 		     }
		     
// 		     return session;
			
// 		}

// 		public Session getBrowse_geoweaver_session() {
			
// 			return findOpenSession();
// 		}

// 		public void setBrowse_geoweaver_session(Session browse_geoweaver_session) {
			
// 			this.browse_geoweaver_session = browse_geoweaver_session;
			
// 		}

// 		public Java2JupyterClientEndpoint getGeoweaver_jupyter_client() {
// 			return geoweaver_jupyter_client;
// 		}

// 		public void setGeoweaver_jupyter_client(Java2JupyterClientEndpoint geoweaver_jupyter_client) {
// 			this.geoweaver_jupyter_client = geoweaver_jupyter_client;
// 		}
    	
//     }


}
