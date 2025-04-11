package com.gw.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for Geoweaver communication settings.
 * This class manages the configuration for communication channels between client and server.
 */
@Configuration
public class CommunicationConfig {
    
    private static final Logger logger = LoggerFactory.getLogger(CommunicationConfig.class);
    
    /**
     * The default communication channel to use.
     * Possible values: "websocket" or "polling" (default)
     */
    @Value("${geoweaver.communication.default-channel:polling}")
    private String defaultChannel;
    
    /**
     * Get the configured default communication channel.
     * 
     * @return The default communication channel ("websocket" or "polling")
     */
    public String getDefaultChannel() {
        logger.debug("Using communication channel: {}", defaultChannel);
        return defaultChannel;
    }
    
    /**
     * Check if WebSocket is the default communication channel.
     * 
     * @return true if WebSocket is the default, false otherwise
     */
    public boolean isWebSocketDefault() {
        return "websocket".equalsIgnoreCase(defaultChannel);
    }
    
    /**
     * Check if HTTP long polling is the default communication channel.
     * 
     * @return true if polling is the default, false otherwise
     */
    public boolean isPollingDefault() {
        return "polling".equalsIgnoreCase(defaultChannel);
    }
}