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
import org.springframework.beans.factory.annotation.Autowired;

import com.gw.jpa.Host;
import com.gw.tools.HostTool;
import com.gw.tools.JupyterSessionPairTool;
import com.gw.tools.SessionPair;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;

/**
 * 
 * This works for redirecting all the jupyter hub traffic
 * 
 * @author JensenSun
 *
 */
//ws://localhost:8070/Geoweaver/jupyter-socket/gedv82/api/kernels/fc43c1dc-67b3-404c-824d-83db95f642cb/channels?session_id=e4144eb8945047aa84027cd9a2eeadc5
//ws://localhost:8070/Geoweaver/jupyter-socket/4g75h7/user/zsun/api/kernels/eaa5f686-0df2-4f39-ae95-d6715d9f7fc5/channels?session_id=dda9fa014a25401894485df465ce90df
@ServerEndpoint(value = "/jupyter-socket/{hostid}/user/{uname}/api/kernels/{uuid1}/channels", 
	configurator = JupyterRedirectServerConfig.class)
public class JupyterHubRedirectServlet{
	
	Logger logger = LoggerFactory.getLogger(getClass());
	
//	Session wsSession;
	
//	@Autowired
//	Java2JupyterClientEndpoint client;
	
	// public static List<SessionPair> pairs = new ArrayList();
	
	// @Autowired
	HostTool ht;
	
	// @Autowired
	BaseTool bt;

	
	public JupyterHubRedirectServlet() {
		
		logger.debug("Initializing JupyterHub Websocket Session...");
		
	}
	
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
    public void open(Session session, 
    		@PathParam("hostid") String hostid, 
    		@PathParam("uname") String username, 
    		@PathParam("uuid1") String uuid1, 
    		EndpointConfig config) {
		
		try {
			
			logger.debug("Enter...");
			
			init(session);
			
			logger.debug("websocket channel to host "+ hostid +" openned");
			
			Host h = ht.getHostById(hostid);
			
			String[] hh = bt.parseJupyterURL(h.getUrl());
			
			String wsprotocol = "ws";
			
			String trueurl = wsprotocol + "://" + hh[1] + ":" + hh[2] + "/user/" + username +
					
					"/api/kernels/"+uuid1+"/channels?" + session.getQueryString();
			
			logger.debug("Query String: " + trueurl);
			
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
    
    
    
    
}
