package com.gw.tools;

import com.gw.tools.AsyncExportManager;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsyncExportManagerTest {

    @Mock
    private WorkflowTool workflowTool;

    @Mock
    private BaseTool baseTool;

    @InjectMocks
    private AsyncExportManager asyncExportManager;

    @BeforeEach
    void setUp() {
        // Reset any static state if needed
    }

    @Test
    @Timeout(10)
    void testStartAsyncExport() throws Exception {
        // Given
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";
        String expectedDownloadUrl = "download/temp/workflow123.zip";

        when(workflowTool.download(workflowId, option)).thenReturn(expectedDownloadUrl);

        // When
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);

        // Then
        assertNotNull(taskId);
        assertEquals(20, taskId.length()); // RandomString(20) length

        // Wait a bit for async task to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Verify the task was created
        AsyncExportManager.ExportTask task = asyncExportManager.getExportTask(taskId);
        assertNotNull(task);
        assertEquals(workflowId, task.getWorkflowId());
        assertEquals(userId, task.getUserId());
        assertEquals(option, task.getOption());
    }

    @Test
    @Timeout(10)
    void testGetExportTask() {
        // Given
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);

        // When
        AsyncExportManager.ExportTask task = asyncExportManager.getExportTask(taskId);

        // Then
        assertNotNull(task);
        assertEquals(taskId, task.getTaskId());
        assertEquals(workflowId, task.getWorkflowId());
        assertEquals(userId, task.getUserId());
        assertEquals(option, task.getOption());
    }

    @Test
    @Timeout(10)
    void testGetExportTaskNotFound() {
        // Given
        String nonExistentTaskId = "nonexistent123";

        // When
        AsyncExportManager.ExportTask task = asyncExportManager.getExportTask(nonExistentTaskId);

        // Then
        assertNull(task);
    }

    @Test
    @Timeout(10)
    void testGetUserExportTasks() {
        // Given
        String userId1 = "user123";
        String userId2 = "user456";
        String workflowId1 = "workflow123";
        String workflowId2 = "workflow456";
        String option = "workflowwithprocesscodehistory";

        // Create tasks for different users
        String taskId1 = asyncExportManager.startAsyncExport(workflowId1, userId1, option);
        String taskId2 = asyncExportManager.startAsyncExport(workflowId2, userId2, option);
        String taskId3 = asyncExportManager.startAsyncExport("workflow789", userId1, option);

        // When
        ConcurrentHashMap<String, AsyncExportManager.ExportTask> userTasks = 
            asyncExportManager.getUserExportTasks(userId1);

        // Then
        assertNotNull(userTasks);
        assertEquals(2, userTasks.size());
        assertTrue(userTasks.containsKey(taskId1));
        assertTrue(userTasks.containsKey(taskId3));
        assertFalse(userTasks.containsKey(taskId2));
    }

    @Test
    @Timeout(10)
    void testGetUserExportTasksEmpty() {
        // Given
        String userId = "user123";

        // When
        ConcurrentHashMap<String, AsyncExportManager.ExportTask> userTasks = 
            asyncExportManager.getUserExportTasks(userId);

        // Then
        assertNotNull(userTasks);
        assertTrue(userTasks.isEmpty());
    }

    @Test
    @Timeout(10)
    void testExportTaskStatusTransitions() throws Exception {
        // Given — block download until we have observed PENDING
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";
        java.util.concurrent.CountDownLatch proceed =
            new java.util.concurrent.CountDownLatch(1);

        when(workflowTool.download(workflowId, option)).thenAnswer(invocation -> {
            proceed.await(5, java.util.concurrent.TimeUnit.SECONDS);
            return "download/temp/workflow123.zip";
        });

        // When
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);

        // Then — wait until worker has set PROCESSING (or still PENDING briefly)
        AsyncExportManager.ExportTask task = null;
        for (int i = 0; i < 50; i++) {
            task = asyncExportManager.getExportTask(taskId);
            assertNotNull(task);
            if (task.getStatus() == AsyncExportManager.ExportStatus.PENDING
                || task.getStatus() == AsyncExportManager.ExportStatus.PROCESSING) {
                break;
            }
            Thread.sleep(10);
        }
        assertNotNull(task);
        assertTrue(
            task.getStatus() == AsyncExportManager.ExportStatus.PENDING
                || task.getStatus() == AsyncExportManager.ExportStatus.PROCESSING,
            "Expected PENDING or PROCESSING before download finishes, was: " + task.getStatus());

        // Allow download to complete and wait for COMPLETED
        proceed.countDown();
        for (int i = 0; i < 50; i++) {
            task = asyncExportManager.getExportTask(taskId);
            if (task.getStatus() == AsyncExportManager.ExportStatus.COMPLETED
                || task.getStatus() == AsyncExportManager.ExportStatus.FAILED) {
                break;
            }
            Thread.sleep(20);
        }
        assertEquals(AsyncExportManager.ExportStatus.COMPLETED, task.getStatus());
    }

    @Test
    @Timeout(10)
    void testExportTaskWithException() throws Exception {
        // Given
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";

        when(workflowTool.download(workflowId, option)).thenThrow(new RuntimeException("Export failed"));

        // When
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);

        // Wait for async task to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        AsyncExportManager.ExportTask task = asyncExportManager.getExportTask(taskId);
        assertNotNull(task);
        assertEquals(AsyncExportManager.ExportStatus.FAILED, task.getStatus());
        assertNotNull(task.getErrorMessage());
        assertTrue(task.getErrorMessage().contains("Export failed"));
    }

    @Test
    @Timeout(10)
    void testExportTaskWithNullDownloadUrl() throws Exception {
        // Given
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";

        when(workflowTool.download(workflowId, option)).thenReturn(null);

        // When
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);

        // Wait for async task to complete
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Then
        AsyncExportManager.ExportTask task = asyncExportManager.getExportTask(taskId);
        assertNotNull(task);
        assertEquals(AsyncExportManager.ExportStatus.FAILED, task.getStatus());
        assertNotNull(task.getErrorMessage());
        assertTrue(task.getErrorMessage().contains("Export failed"));
    }

    @Test
    @Timeout(10)
    void testExportTaskGettersAndSetters() {
        // Given
        String taskId = "task123";
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";

        // When
        AsyncExportManager.ExportTask task = new AsyncExportManager.ExportTask(taskId, workflowId, userId, option);

        // Then
        assertEquals(taskId, task.getTaskId());
        assertEquals(workflowId, task.getWorkflowId());
        assertEquals(userId, task.getUserId());
        assertEquals(option, task.getOption());
        assertEquals(AsyncExportManager.ExportStatus.PENDING, task.getStatus());
        assertNull(task.getDownloadUrl());
        assertNull(task.getErrorMessage());
        assertNull(task.getCompletedAt());
        assertNotNull(task.getCreatedAt());

        // Test setters
        task.setStatus(AsyncExportManager.ExportStatus.COMPLETED);
        task.setDownloadUrl("download/temp/test.zip");
        task.setErrorMessage("Test error");
        task.setCompletedAt(new java.util.Date());

        assertEquals(AsyncExportManager.ExportStatus.COMPLETED, task.getStatus());
        assertEquals("download/temp/test.zip", task.getDownloadUrl());
        assertEquals("Test error", task.getErrorMessage());
        assertNotNull(task.getCompletedAt());
    }

    @Test
    @Timeout(10)
    void testCleanupExpiredTasks() {
        // Given
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";

        // Create a task
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);
        AsyncExportManager.ExportTask task = asyncExportManager.getExportTask(taskId);
        
        // Manually set the task as very old (simulate expired task)
        task.setCreatedAt(new java.util.Date(System.currentTimeMillis() - 25 * 60 * 60 * 1000)); // 25 hours ago

        // When
        asyncExportManager.cleanupExpiredTasks();

        // Then
        AsyncExportManager.ExportTask expiredTask = asyncExportManager.getExportTask(taskId);
        assertNull(expiredTask);
    }

    @Test
    @Timeout(10)
    void testShutdown() {
        // Given
        String workflowId = "workflow123";
        String userId = "user123";
        String option = "workflowwithprocesscodehistory";

        // Create a task
        String taskId = asyncExportManager.startAsyncExport(workflowId, userId, option);

        // When
        asyncExportManager.shutdown();

        // Then
        // The executor service should be shut down
        // This test mainly ensures no exceptions are thrown during shutdown
        assertNotNull(taskId);
    }
}
