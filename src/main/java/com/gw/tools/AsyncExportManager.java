package com.gw.tools;

import com.gw.jpa.Workflow;
import com.gw.jpa.GWUser;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;

/**
 * Async export manager for handling asynchronous workflow export tasks
 */
@Service
public class AsyncExportManager {
    
    private static final Logger logger = LoggerFactory.getLogger(AsyncExportManager.class);
    
    @Autowired
    private WorkflowTool workflowTool;
    
    @Autowired
    private BaseTool baseTool;
    
    // Export task status enum
    public enum ExportStatus {
        PENDING,    // Waiting
        PROCESSING, // Processing
        COMPLETED,  // Completed
        FAILED      // Failed
    }
    
    // Export task information
    public static class ExportTask {
        private String taskId;
        private String workflowId;
        private String userId;
        private String option;
        private ExportStatus status;
        private String downloadUrl;
        private String errorMessage;
        private Date createdAt;
        private Date completedAt;
        
        public ExportTask(String taskId, String workflowId, String userId, String option) {
            this.taskId = taskId;
            this.workflowId = workflowId;
            this.userId = userId;
            this.option = option;
            this.status = ExportStatus.PENDING;
            this.createdAt = new Date();
        }
        
        // Getters and Setters
        public String getTaskId() { return taskId; }
        public void setTaskId(String taskId) { this.taskId = taskId; }
        
        public String getWorkflowId() { return workflowId; }
        public void setWorkflowId(String workflowId) { this.workflowId = workflowId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getOption() { return option; }
        public void setOption(String option) { this.option = option; }
        
        public ExportStatus getStatus() { return status; }
        public void setStatus(ExportStatus status) { this.status = status; }
        
        public String getDownloadUrl() { return downloadUrl; }
        public void setDownloadUrl(String downloadUrl) { this.downloadUrl = downloadUrl; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Date getCreatedAt() { return createdAt; }
        public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
        
        public Date getCompletedAt() { return completedAt; }
        public void setCompletedAt(Date completedAt) { this.completedAt = completedAt; }
    }
    
    // Map to store export tasks
    private final ConcurrentHashMap<String, ExportTask> exportTasks = new ConcurrentHashMap<>();
    
    // Thread pool for async processing
    private final ExecutorService executorService = Executors.newFixedThreadPool(5);
    
    /**
     * Start async export task
     * @param workflowId Workflow ID
     * @param userId User ID
     * @param option Export option
     * @return Task ID
     */
    public String startAsyncExport(String workflowId, String userId, String option) {
        String taskId = RandomString.get(20);
        
        ExportTask task = new ExportTask(taskId, workflowId, userId, option);
        exportTasks.put(taskId, task);
        
        // Submit async task
        executorService.submit(() -> {
            try {
                task.setStatus(ExportStatus.PROCESSING);
                logger.info("Starting async export workflow: {} Task ID: {}", workflowId, taskId);
                
                // Execute actual export logic
                String downloadUrl = workflowTool.download(workflowId, option);
                
                if (downloadUrl != null) {
                    task.setDownloadUrl(downloadUrl);
                    task.setStatus(ExportStatus.COMPLETED);
                    task.setCompletedAt(new Date());
                    logger.info("Workflow export completed: {} Task ID: {} Download URL: {}", workflowId, taskId, downloadUrl);
                } else {
                    task.setStatus(ExportStatus.FAILED);
                    task.setErrorMessage("Export failed: Unable to generate download file");
                    task.setCompletedAt(new Date());
                    logger.error("Workflow export failed: {} Task ID: {}", workflowId, taskId);
                }
            } catch (Exception e) {
                task.setStatus(ExportStatus.FAILED);
                task.setErrorMessage("Error occurred during export: " + e.getMessage());
                task.setCompletedAt(new Date());
                logger.error("Workflow export exception: {} Task ID: {} Error: {}", workflowId, taskId, e.getMessage(), e);
            }
        });
        
        return taskId;
    }
    
    /**
     * Get export task status
     * @param taskId Task ID
     * @return Export task information
     */
    public ExportTask getExportTask(String taskId) {
        return exportTasks.get(taskId);
    }
    
    /**
     * Get all export tasks for a user
     * @param userId User ID
     * @return Export task list
     */
    public ConcurrentHashMap<String, ExportTask> getUserExportTasks(String userId) {
        ConcurrentHashMap<String, ExportTask> userTasks = new ConcurrentHashMap<>();
        for (ExportTask task : exportTasks.values()) {
            if (userId.equals(task.getUserId())) {
                userTasks.put(task.getTaskId(), task);
            }
        }
        return userTasks;
    }
    
    /**
     * Clean up expired export tasks (over 24 hours)
     */
    public void cleanupExpiredTasks() {
        long expireTime = 24 * 60 * 60 * 1000; // 24 hours
        long currentTime = System.currentTimeMillis();
        
        exportTasks.entrySet().removeIf(entry -> {
            ExportTask task = entry.getValue();
            return (currentTime - task.getCreatedAt().getTime()) > expireTime;
        });
    }
    
    /**
     * 关闭线程池
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
