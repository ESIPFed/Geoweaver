package com.gw.server;

import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

@ServerEndpoint(value = "/test-socket2/{hostid}/api/kernels/{uuid1}/channels")
public class Test2SocketServlet {
	
	public Test2SocketServlet() {
		
		System.out.println("Test2 websocket servlet is created!");
		
	}
	
	@OnOpen
    public void open(Session session, 
    		@PathParam("hostid") String hostid, 
    		@PathParam("uuid1") String uuid1, 
    		EndpointConfig config) {
		
		
    }

    @OnError
    public void error(final Session session, final Throwable throwable) throws Throwable {
    	
    	
    }

    @OnMessage(maxMessageSize = 10000000)
    public void echo(String message, @PathParam("uuid1") String uuid1, Session session) {
        
    	
    }

    @OnClose
    public void close(final Session session) {
    	
    	
    }

}
