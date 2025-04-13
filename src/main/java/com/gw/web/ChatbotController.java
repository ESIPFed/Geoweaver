package com.gw.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tools.HistoryTool;
import com.gw.tools.ProcessTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for handling chatbot interactions.
 * This controller provides endpoints for the AI assistant functionality in Geoweaver.
 */
@RestController
@RequestMapping("/api/chatbot")
public class ChatbotController {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotController.class);

    @Autowired
    private ProcessTool processTool;

    @Autowired
    private WorkflowTool workflowTool;

    @Autowired
    private HistoryTool historyTool;

    @Autowired
    private BaseTool baseTool;

    @Value("${geoweaver.llm.api_url:}")
    private String llmApiUrl;

    @Value("${geoweaver.llm.api_key:}")
    private String llmApiKey;

    @Value("${geoweaver.llm.model:gpt-3.5-turbo}")
    private String llmModel;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * Endpoint to check if the LLM API is configured.
     * 
     * @return ResponseEntity with a boolean indicating if the LLM API is configured
     */
    @GetMapping("/config-status")
    public ResponseEntity<?> getConfigStatus() {
        Map<String, Object> response = new HashMap<>();
        boolean isConfigured = llmApiUrl != null && !llmApiUrl.isEmpty();
        response.put("configured", isConfigured);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint for sending messages to the chatbot.
     * 
     * @param request The chatbot request containing the user message and context
     * @return ResponseEntity with the chatbot's response
     */
    @PostMapping("/message")
    public ResponseEntity<?> sendMessage(@RequestBody ChatbotRequest request) {
        try {
            logger.info("Received chatbot message: {}", request.getMessage());
            
            // Prepare response
            Map<String, Object> response = new HashMap<>();
            
            // If LLM API URL is not configured, use a fallback response
            if (llmApiUrl == null || llmApiUrl.isEmpty()) {
                response.put("message", "I'm sorry, the AI assistant is not fully configured. Please set the LLM API URL in the application properties.");
                return ResponseEntity.ok(response);
            }
            
            // Process the message with the LLM
            String llmResponse = processWithLLM(request);
            
            // Parse the LLM response
            Map<String, Object> parsedResponse = parseResponse(llmResponse);
            
            // Execute any actions if needed
            if (parsedResponse.containsKey("actions")) {
                List<Map<String, Object>> actions = (List<Map<String, Object>>) parsedResponse.get("actions");
                executeActions(actions);
                response.put("actions", actions);
            }
            
            // Set the response message
            response.put("message", parsedResponse.getOrDefault("message", "I processed your request."));
            
            // Include any metrics if available
            if (parsedResponse.containsKey("metrics")) {
                response.put("metrics", parsedResponse.get("metrics"));
            }
            
            // Update workflow/model IDs if provided
            if (request.getWorkflowId() != null) {
                response.put("workflowId", request.getWorkflowId());
            }
            
            if (request.getModelId() != null) {
                response.put("modelId", request.getModelId());
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error processing chatbot message", e);
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Sorry, I encountered an error: " + e.getMessage());
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Process the user message with an LLM service.
     * 
     * @param request The chatbot request
     * @return The LLM's response
     */
    private String processWithLLM(ChatbotRequest request) {
        try {
            // Prepare context for the LLM
            Map<String, Object> context = new HashMap<>();
            
            // Add workflow information if available
            if (request.getWorkflowId() != null) {
                Workflow workflow = workflowTool.getById(request.getWorkflowId());
                if (workflow != null) {
                    context.put("workflow", workflow);
                    
                    // Get workflow history if available
                    List<History> workflowHistory = historyTool.getHistoryByWorkflowId(request.getWorkflowId());
                    if (workflowHistory != null && !workflowHistory.isEmpty()) {
                        context.put("workflowHistory", workflowHistory);
                    }
					
                }
            }
            
            // Add model information if available
            if (request.getModelId() != null) {
                GWProcess process = processTool.getProcessById(request.getModelId());
                if (process != null) {
					// not model, should be process code
                    context.put("model", process);
                    
                    // Get process history if available
                    // List<History> processHistory = historyTool.getHistoryByProcess(request.getModelId());
                    // if (processHistory != null && !processHistory.isEmpty()) {
                    //     context.put("modelHistory", processHistory);
                    // }
                }
            }
            
            // Prepare the request to the LLM API
            Map<String, Object> llmRequest = new HashMap<>();
            llmRequest.put("message", request.getMessage());
            llmRequest.put("context", context);
            llmRequest.put("conversation_history", request.getConversationHistory());
            
            // Set up headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (llmApiKey != null && !llmApiKey.isEmpty()) {
                headers.set("Authorization", "Bearer " + llmApiKey);
            }
            
            // Make the request to the LLM API
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(llmRequest, headers);
            ResponseEntity<String> llmResponseEntity = restTemplate.postForEntity(llmApiUrl, entity, String.class);
            
            return llmResponseEntity.getBody();
            
        } catch (Exception e) {
            logger.error("Error communicating with LLM API", e);
            return "{\"message\":\"I'm sorry, I encountered an error while processing your request. Please try again later.\"}";
        }
    }

    /**
     * Parse the LLM response into a structured format.
     * 
     * @param response The raw response from the LLM
     * @return A map containing the parsed response
     */
    private Map<String, Object> parseResponse(String response) {
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            logger.error("Error parsing LLM response", e);
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("message", response);
            return fallback;
        }
    }

    /**
     * Execute actions requested by the LLM.
     * 
     * @param actions List of actions to execute
     */
    private void executeActions(List<Map<String, Object>> actions) {
        for (Map<String, Object> action : actions) {
            try {
                String type = (String) action.get("type");
                
                switch (type) {
                    case "run_workflow":
                        String workflowId = (String) action.get("workflowId");
                        if (workflowId != null) {
                            // Execute workflow
                            // Note: This is a simplified example. In a real implementation,
                            // you would need to handle authentication, hosts, etc.
                            String historyId = RandomString.get(12);
                            workflowTool.execute(historyId, workflowId, "1", new String[]{}, new String[]{}, new String[]{}, "");
                        }
                        break;
                        
                    case "modify_process":
                        String processId = (String) action.get("processId");
                        String code = (String) action.get("code");
                        if (processId != null && code != null) {
                            // Update process code
                            GWProcess process = processTool.getProcessById(processId);
                            if (process != null) {
                                process.setCode(code);
                                processTool.save(process);
                            }
                        }
                        break;
                        
                    case "create_process":
                        // Implementation for creating a new process
                        // This would involve creating a new GWProcess object and saving it
                        break;
                        
                    default:
                        logger.warn("Unknown action type: {}", type);
                }
                
            } catch (Exception e) {
                logger.error("Error executing action", e);
            }
        }
    }

    /**
     * Request object for chatbot interactions.
     */
    public static class ChatbotRequest {
        private String message;
        private String workflowId;
        private String modelId;
        private List<Map<String, Object>> conversationHistory = new ArrayList<>();

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        public void setWorkflowId(String workflowId) {
            this.workflowId = workflowId;
        }

        public String getModelId() {
            return modelId;
        }

        public void setModelId(String modelId) {
            this.modelId = modelId;
        }

        public List<Map<String, Object>> getConversationHistory() {
            return conversationHistory;
        }

        public void setConversationHistory(List<Map<String, Object>> conversationHistory) {
            this.conversationHistory = conversationHistory;
        }
    }
}