package com.gw.server;

import javax.servlet.http.HttpSession;
import javax.websocket.HandshakeResponse;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.log4j.Logger;

/**
 * 
 * Jupyter WebSocket
 * 
 * @author JensenSun
 *
 */

public class JupyterRedirectServerConfig extends ServerEndpointConfig.Configurator  {
	
	Logger logger = Logger.getLogger(this.getClass());
//public class JupyterRedirectServerConfig extends SpringConfigurator  {
 
//	@Bean
//    public ServletServerContainerFactoryBean createWebSocketContainer() {
//		System.out.print("WebSocket container is created ");
//        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
////        container.setMaxBinaryMessageBufferSize(1024000);
//        return container;
//    }
//
//    @Override
//    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
//    	System.out.print("WebSocket handler is registered for URL mapping /socket ");
//        registry.addHandler(new JupyterHandler(), "/socket").setAllowedOrigins("*");
//    }
	@Override
    public void modifyHandshake(ServerEndpointConfig config, 
                                HandshakeRequest request, 
                                HandshakeResponse response)
    {
        HttpSession httpSession = (HttpSession)request.getHttpSession();
        
        logger.debug("Received Handshake Request Headers: " + request.getHeaders());
        
        logger.debug("HttpSession Configure User Properties: " + config.getUserProperties());
        
        config.getUserProperties().put("RequestHeaders", request.getHeaders());
        
//        config.getUserProperties().put(HttpSession.class.getName(),httpSession);
    }

}
