package com.gw.server;

import com.gw.utils.BaseTool;
import com.gw.utils.ExecutionLogBroker;
import com.gw.utils.MessageQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controller for HTTP long polling as a fallback when WebSocket connections fail.
 * This provides an alternative communication channel for clients that cannot establish
 * or maintain WebSocket connections (e.g., due to proxy issues).
 */
@RestController
@RequestMapping("/api/longpoll")
public class LongPollingController {

    private static final Logger logger = LoggerFactory.getLogger(LongPollingController.class);
    
    @Autowired
    private MessageQueue messageQueue;

    @Autowired
    private ExecutionLogBroker executionLogBroker;
    
    // Map to store pending requests
    private final Map<String, DeferredResult<ResponseEntity<?>>> pendingRequests = new ConcurrentHashMap<>();
    
    // Timeout for long polling requests (in milliseconds)
    private static final long LONG_POLL_TIMEOUT = 30000; // 30 seconds
    
    /**
     * Endpoint for clients to poll for messages.
     * If messages are available, they are returned immediately.
     * If no messages are available, the request is held until messages arrive or timeout occurs.
     * 
     * @param token The client token
     * @return DeferredResult containing messages or timeout status
     */
    @GetMapping("/poll/{token}")
    public DeferredResult<ResponseEntity<?>> pollMessages(@PathVariable String token) {
        logger.debug("Received long polling request for token: {}", token);
        
        DeferredResult<ResponseEntity<?>> result = new DeferredResult<>(LONG_POLL_TIMEOUT);
        
        // Check if there are already messages in the queue
        if (messageQueue.hasMessages(token)) {
            String[] messages = messageQueue.getAndClearMessages(token);
            result.setResult(ResponseEntity.ok(messages));
            return result;
        }
        
        // Store the pending request
        pendingRequests.put(token, result);
        
        // Set timeout callback
        result.onTimeout(() -> {
            pendingRequests.remove(token);
            result.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).body(null));
            logger.debug("Long polling request timed out for token: {}", token);
        });
        
        // Set completion callback to clean up resources
        result.onCompletion(() -> {
            pendingRequests.remove(token);
        });
        
        return result;
    }
    
    /**
     * Endpoint for clients to send control messages (token registration, execution subscribe)
     * or for internal use. Client commands such as {@code token:}, {@code execution:}, and
     * {@code subscribe:} are handled server-side and are not echoed into the caller's queue.
     * 
     * @param token The client token
     * @param message The message to send
     * @return Status of the operation
     */
    @PostMapping("/send/{token}")
    public ResponseEntity<?> sendMessage(
            @PathVariable String token, 
            @RequestBody String message) {
        
        logger.debug("Sending message to token: {}", token);

        if (handleClientCommand(token, message)) {
            return ResponseEntity.ok().build();
        }
        
        // Check if there's a pending request for this token
        DeferredResult<ResponseEntity<?>> pendingRequest = pendingRequests.remove(token);
        
        if (pendingRequest != null && !pendingRequest.isSetOrExpired()) {
            // If there's a pending request, complete it with the message
            pendingRequest.setResult(ResponseEntity.ok(new String[]{message}));
            return ResponseEntity.ok().build();
        } else {
            // Otherwise, queue the message for future polling
            boolean added = messageQueue.addMessage(token, message);
            if (added) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.INSUFFICIENT_STORAGE)
                        .body("Failed to queue message: queue full or other error");
            }
        }
    }

    /**
     * Handle client control commands posted to the long-poll send endpoint.
     *
     * @return true if the message was a handled control command (do not queue/echo)
     */
    private boolean handleClientCommand(String token, String message) {
        if (BaseTool.isNull(message)) {
            return false;
        }
        String trimmed = message.trim();
        if (trimmed.startsWith("token:")) {
            // Registration is implicit via the path token; acknowledge without echoing
            logger.debug("Long-poll client registered token: {}", token);
            return true;
        }
        if (trimmed.startsWith("execution:") || trimmed.startsWith("subscribe:")) {
            String historyId = trimmed.substring(10);
            if (!BaseTool.isNull(historyId) && executionLogBroker != null) {
                executionLogBroker.subscribe(historyId, token);
                // Confirm subscription on this tab's feed
                sendMessageToClient(
                    token,
                    historyId
                        + BaseTool.log_separator
                        + "Subscribed to live execution logs: "
                        + historyId);
            }
            return true;
        }
        if (trimmed.startsWith("unsubscribe:")) {
            String historyId = trimmed.substring(12);
            if (!BaseTool.isNull(historyId) && executionLogBroker != null) {
                executionLogBroker.unsubscribe(historyId, token);
            }
            return true;
        }
        return false;
    }
    
    /**
     * Utility method to send a message to a client via long polling.
     * This is used by other components to send messages when WebSocket is unavailable.
     * 
     * @param token The client token
     * @param message The message to send
     * @return true if the message was sent or queued successfully, false otherwise
     */
    public boolean sendMessageToClient(String token, String message) {
        if (BaseTool.isNull(token) || BaseTool.isNull(message)) {
            logger.warn("Attempted to send null token or message");
            return false;
        }
        
        // Check if there's a pending request for this token
        DeferredResult<ResponseEntity<?>> pendingRequest = pendingRequests.remove(token);
        
        if (pendingRequest != null && !pendingRequest.isSetOrExpired()) {
            // If there's a pending request, complete it with the message
            pendingRequest.setResult(ResponseEntity.ok(new String[]{message}));
            return true;
        } else {
            // Otherwise, queue the message for future polling
            return messageQueue.addMessage(token, message);
        }
    }
}
