package com.gw.utils;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * A message queue for storing messages when WebSocket is unavailable.
 * This provides a fallback mechanism for communication when WebSocket connections fail.
 */
@Component
public class MessageQueue {

    private static final Logger logger = LoggerFactory.getLogger(MessageQueue.class);
    
    // Map of token to queue of messages
    private final Map<String, Queue<String>> messageQueues = new ConcurrentHashMap<>();
    
    // Maximum number of messages to store per token
    private static final int MAX_QUEUE_SIZE = 1000;
    
    /**
     * Add a message to the queue for a specific token.
     * 
     * @param token The token identifying the client
     * @param message The message to queue
     * @return true if the message was added, false if the queue is full
     */
    public boolean addMessage(String token, String message) {
        if (token == null || message == null) {
            logger.warn("Attempted to add null token or message to queue");
            return false;
        }
        
        Queue<String> queue = messageQueues.computeIfAbsent(token, k -> new ConcurrentLinkedQueue<>());
        
        // Check if queue has reached maximum size
        if (queue.size() >= MAX_QUEUE_SIZE) {
            logger.warn("Message queue for token {} is full", token);
            return false;
        }
        
        return queue.offer(message);
    }
    
    /**
     * Get and remove all messages for a specific token.
     * 
     * @param token The token identifying the client
     * @return Array of messages, or empty array if no messages
     */
    public String[] getAndClearMessages(String token) {
        if (token == null) {
            logger.warn("Attempted to get messages for null token");
            return new String[0];
        }
        
        Queue<String> queue = messageQueues.get(token);
        if (queue == null || queue.isEmpty()) {
            return new String[0];
        }
        
        String[] messages = queue.toArray(new String[0]);
        queue.clear();
        return messages;
    }
    
    /**
     * Get all messages for a specific token without removing them.
     * 
     * @param token The token identifying the client
     * @return Array of messages, or empty array if no messages
     */
    public String[] peekMessages(String token) {
        if (token == null) {
            logger.warn("Attempted to peek messages for null token");
            return new String[0];
        }
        
        Queue<String> queue = messageQueues.get(token);
        if (queue == null || queue.isEmpty()) {
            return new String[0];
        }
        
        return queue.toArray(new String[0]);
    }
    
    /**
     * Check if there are any messages for a specific token.
     * 
     * @param token The token identifying the client
     * @return true if there are messages, false otherwise
     */
    public boolean hasMessages(String token) {
        if (token == null) {
            return false;
        }
        
        Queue<String> queue = messageQueues.get(token);
        return queue != null && !queue.isEmpty();
    }
    
    /**
     * Clear all messages for a specific token.
     * 
     * @param token The token identifying the client
     */
    public void clearMessages(String token) {
        if (token == null) {
            return;
        }
        
        Queue<String> queue = messageQueues.get(token);
        if (queue != null) {
            queue.clear();
        }
    }
    
    /**
     * Get the number of messages for a specific token.
     * 
     * @param token The token identifying the client
     * @return Number of messages
     */
    public int getMessageCount(String token) {
        if (token == null) {
            return 0;
        }
        
        Queue<String> queue = messageQueues.get(token);
        return queue != null ? queue.size() : 0;
    }
}