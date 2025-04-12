package com.gw.utils;

import com.gw.jpa.ExecutionStatus;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A cache service for storing process execution status in memory.
 * This reduces database queries when checking process status and helps avoid race conditions.
 */
@Service
public class ProcessStatusCache {

    private static final Logger logger = Logger.getLogger(ProcessStatusCache.class);
    
    // Cache to store history ID -> status mapping
    private final Map<String, String> statusCache = new ConcurrentHashMap<>();
    
    // Cache to store history ID -> timestamp mapping for cleanup
    private final Map<String, Long> timestampCache = new ConcurrentHashMap<>();
    
    // Default expiration time in milliseconds (30 minutes)
    private static final long DEFAULT_EXPIRATION_TIME = 30 * 60 * 1000;
    
    private final ScheduledExecutorService cleanupExecutor;
    
    public ProcessStatusCache() {
        // Initialize the cleanup scheduler
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
        
        // Schedule cleanup task to run every 5 minutes
        cleanupExecutor.scheduleAtFixedRate(
            this::cleanupExpiredEntries, 
            5, 
            5, 
            TimeUnit.MINUTES
        );
        
        logger.info("Process status cache initialized");
    }
    
    /**
     * Update the status of a process in the cache
     * 
     * @param historyId The history ID
     * @param status The current execution status
     */
    public void updateStatus(String historyId, String status) {
        if (historyId == null || status == null) {
            return;
        }
        
        statusCache.put(historyId, status);
        timestampCache.put(historyId, System.currentTimeMillis());
        
        logger.debug("Updated cache for history ID: " + historyId + " with status: " + status);
    }
    
    /**
     * Get the status of a process from the cache
     * 
     * @param historyId The history ID
     * @return The execution status or null if not found in cache
     */
    public String getStatus(String historyId) {
        if (historyId == null) {
            return null;
        }
        
        String status = statusCache.get(historyId);
        
        // Update timestamp if entry exists to prevent premature expiration
        if (status != null) {
            timestampCache.put(historyId, System.currentTimeMillis());
        }
        
        return status;
    }
    
    /**
     * Remove a process from the cache
     * 
     * @param historyId The history ID
     */
    public void removeStatus(String historyId) {
        if (historyId == null) {
            return;
        }
        
        statusCache.remove(historyId);
        timestampCache.remove(historyId);
    }
    
    /**
     * Check if a process is in the cache
     * 
     * @param historyId The history ID
     * @return True if the process is in the cache, false otherwise
     */
    public boolean containsStatus(String historyId) {
        return historyId != null && statusCache.containsKey(historyId);
    }
    
    /**
     * Clean up expired entries from the cache
     */
    private void cleanupExpiredEntries() {
        long currentTime = System.currentTimeMillis();
        
        timestampCache.entrySet().removeIf(entry -> {
            String historyId = entry.getKey();
            Long timestamp = entry.getValue();
            
            if (currentTime - timestamp > DEFAULT_EXPIRATION_TIME) {
                statusCache.remove(historyId);
                logger.debug("Removed expired cache entry for history ID: " + historyId);
                return true;
            }
            
            return false;
        });
    }
    
    /**
     * Shutdown the cleanup executor
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
    }
}