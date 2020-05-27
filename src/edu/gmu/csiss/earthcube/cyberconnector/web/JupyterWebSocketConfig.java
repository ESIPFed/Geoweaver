package edu.gmu.csiss.earthcube.cyberconnector.web;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * 
 * Jupyter WebSocket
 * 
 * @author JensenSun
 *
 */
@Configuration
@EnableWebSocketMessageBroker
public class JupyterWebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {
 
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry config) {
//        config.enableSimpleBroker("/topic");
//        config.setApplicationDestinationPrefixes("/app");
//    }
// 
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
//         registry.addEndpoint("/chat");
//         registry.addEndpoint("/chat").withSockJS();
//    }

}
