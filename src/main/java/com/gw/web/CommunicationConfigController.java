package com.gw.web;

import com.gw.utils.CommunicationConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for exposing communication configuration to clients.
 * This allows clients to query the server's preferred communication channel.
 */
@RestController
@RequestMapping("/api/config")
public class CommunicationConfigController {

    @Autowired
    private CommunicationConfig communicationConfig;
    
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
}