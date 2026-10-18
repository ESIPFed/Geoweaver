package com.gw.web;

import com.gw.api.v1.AgentApiProperties;
import com.gw.utils.CommunicationConfig;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for exposing communication configuration to clients.
 * This allows clients to query the server's preferred communication channel.
 */
@RestController
@RequestMapping("/api/config")
public class CommunicationConfigController {

    @Autowired
    private CommunicationConfig communicationConfig;

    @Autowired
    private AgentApiProperties agentApiProperties;
    
    /**
     * Get the server's preferred communication channel.
     * 
     * @return JSON response with the default channel configuration
     */
    @GetMapping("/communication-channel")
    public ResponseEntity<Map<String, String>> getCommunicationChannel() {
        Map<String, String> response = new HashMap<>();
        response.put("defaultChannel", communicationConfig.getDefaultChannel());
        return ResponseEntity.ok(response);
    }

    /**
     * Booleans only for GUI risk banner — never secrets or tokens.
     */
    @GetMapping("/agent-api")
    public ResponseEntity<Map<String, Object>> getAgentApiStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("apiEnabled", agentApiProperties.isApiEnabled());
        response.put("allowLocalhostRuns", agentApiProperties.isAllowLocalhostRuns());
        response.put("allowNonLoopback", agentApiProperties.isAllowNonLoopback());
        response.put("acknowledgeNetworkRisk", agentApiProperties.isAcknowledgeNetworkRisk());
        response.put("boundBeyondLoopback", agentApiProperties.isBoundBeyondLoopback());
        response.put("workspaceEventsEnabled", agentApiProperties.isWorkspaceEventsEnabled());
        response.put(
            "networkRiskActive",
            agentApiProperties.isApiEnabled()
                && agentApiProperties.isBoundBeyondLoopback()
                && agentApiProperties.isAllowNonLoopback());
        return ResponseEntity.ok(response);
    }
}