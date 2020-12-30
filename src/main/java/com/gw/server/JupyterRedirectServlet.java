package com.gw.server;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

import com.gw.jpa.Host;
import com.gw.tools.HostTool;
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
	
	Logger logger = Logger.getLogger(JupyterRedirectServlet.class);
	
	Session wsSession;
	
//	@Autowired
	Java2JupyterClientEndpoint client;
	
//	@Autowired
	HostTool ht;
	
//	@Autowired
	BaseTool bt;
	
	public JupyterRedirectServlet() {
		
		
	}
	
	private void init() {
		
		if (client == null) {
			
			client = BeanTool.getBean(Java2JupyterClientEndpoint.class);
		
		}
		
		if(ht==null) {
			
			ht = BeanTool.getBean(HostTool.class);
			
		}
		
		if(bt==null) {
			
			bt = BeanTool.getBean(BaseTool.class);
			
		}	
		
	}
	
//    private HttpSession httpSession;
	
	@OnOpen
    public void open(Session session, @PathParam("hostid") String hostid, @PathParam("uuid1") String uuid1, EndpointConfig config) {
		
		try {
			
			init();
			
			logger.debug("websocket channel to host "+ hostid +" openned");
			
			Host h = ht.getHostById(hostid);
			
			String[] hh = bt.parseJupyterURL(h.getUrl());
			
			String wsprotocol = "ws";
			
			String trueurl = wsprotocol + "://"+hh[1]+":"+hh[2]+"/api/kernels/"+uuid1+"/channels?" + session.getQueryString();
			
			logger.debug("Query String: " + trueurl);
			
			this.wsSession = session;
			
//			this.httpSession = (HttpSession) config.getUserProperties()
//                    .get(HttpSession.class.getName());
			Map<String, List<String>> headers = (Map<String, List<String>>)config.getUserProperties().get("RequestHeaders");
			
//			client = new Java2JupyterClientEndpoint(new URI(trueurl), session, headers, h);
			
			client.init(new URI(trueurl), session, headers, h);
			
			logger.debug("The connections from javascript end to this servlet, and this servlet to Jupyter server have been created.");
			
		} catch (URISyntaxException e) {
			
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
    		
    		init();
    		
    		if(bt.isNull(client)) {
    			
    			session.close();
    			
    		}else {
    			
    			logger.debug("Received message: " + message);
	        	
	        	logger.debug("UUID string: " + uuid1 + " - Session ID: " + session.getQueryString());
	        	
	        	logger.debug("Transfer message to Jupyter Notebook server..");
	        	
	        	client.sendMessage(message);
	        	
    		}
    		
    	}catch(Exception e) {
    		
    		e.printStackTrace();
    		
    	}
    	
    }

    @OnClose
    public void close(final Session session) {
    	
		try {
			
			init();
			
    		logger.error("Channel closed.");
        	
			client.getNew_ws_session_between_geoweaver_and_jupyterserver().close(); //close websocket connection
			
		} catch (IOException e) {
			
			e.printStackTrace();
			
		}
    	
    }


}
