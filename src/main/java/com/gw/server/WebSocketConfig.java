package com.gw.server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

@Configuration
@EnableWebSocket
public class WebSocketConfig {
	
    @Bean
    public ServerEndpointExporter serverEndpoint() {
        return new ServerEndpointExporter();
    }
    
    @Bean
    public CommandServlet callCommandWebSocketController()
    {
        return new CommandServlet();
    }
    
    @Bean
    public TerminalServlet callTerminalWebSocketController()
    {
        return new TerminalServlet();
    }
    
    @Bean
    public WorkflowServlet callWorkflowWebSocketController()
    {
        return new WorkflowServlet();
    }
    
    @Bean
    public JupyterRedirectServlet callJupyterWebSocketController() {
    	
    	return new JupyterRedirectServlet();
    	
    }
    
    
}
