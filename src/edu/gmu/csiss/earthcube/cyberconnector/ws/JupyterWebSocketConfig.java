package edu.gmu.csiss.earthcube.cyberconnector.ws;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.standard.ServletServerContainerFactoryBean;

/**
 * 
 * Jupyter WebSocket
 * 
 * @author JensenSun
 *
 */
@Configuration
@EnableWebSocket
public class JupyterWebSocketConfig implements WebSocketConfigurer  {
 
	@Bean
    public ServletServerContainerFactoryBean createWebSocketContainer() {
		System.out.print("WebSocket container is created ");
        ServletServerContainerFactoryBean container = new ServletServerContainerFactoryBean();
//        container.setMaxBinaryMessageBufferSize(1024000);
        return container;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
    	System.out.print("WebSocket handler is registered for URL mapping /socket ");
        registry.addHandler(new JupyterHandler(), "/socket").setAllowedOrigins("*");
    }
    

}
