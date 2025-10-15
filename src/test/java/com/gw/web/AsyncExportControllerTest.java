package com.gw.web;

import com.gw.tools.AsyncExportManager;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ModelMap;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AsyncExportControllerTest {

    @Mock
    private AsyncExportManager asyncExportManager;

    @Mock
    private UserTool userTool;

    @Mock
    private BaseTool baseTool;

    @Mock
    private WebRequest webRequest;

    @Mock
    private HttpSession httpSession;

    @Mock
    private HttpServletRequest httpServletRequest;

    @Mock
    private ModelMap modelMap;

    @InjectMocks
    private GeoweaverController geoweaverController;

    @BeforeEach
    void setUp() {
        // No common setup needed - each test sets up its own mocks
    }

    @Test
    @Timeout(10)
    void testAsyncDownloadworkflow() {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscodehistory";
        String taskId = "task12345678901234567890";

        when(httpSession.getId()).thenReturn("session123");
        when(userTool.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
        when(userTool.getAuthUserId("session123", "127.0.0.1")).thenReturn("user123");
        when(webRequest.getParameter("id")).thenReturn(workflowId);
        when(webRequest.getParameter("option")).thenReturn(option);
        when(asyncExportManager.startAsyncExport(workflowId, "user123", option)).thenReturn(taskId);

        // When
        String result = geoweaverController.asyncDownloadworkflow(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("taskId"));
        assertTrue(result.contains("PENDING"));
        assertTrue(result.contains("Export task started"));
        verify(asyncExportManager).startAsyncExport(workflowId, "user123", option);
    }

    @Test
    @Timeout(10)
    void testAsyncDownloadworkflowWithException() {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscodehistory";

        when(httpSession.getId()).thenReturn("session123");
        when(userTool.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
        when(userTool.getAuthUserId("session123", "127.0.0.1")).thenReturn("user123");
        when(webRequest.getParameter("id")).thenReturn(workflowId);
        when(webRequest.getParameter("option")).thenReturn(option);
        when(asyncExportManager.startAsyncExport(workflowId, "user123", option))
            .thenThrow(new RuntimeException("Export failed"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            geoweaverController.asyncDownloadworkflow(modelMap, webRequest, httpSession, httpServletRequest);
        });
    }

    @Test
    @Timeout(10)
    void testGetExportStatus() {
        // Given
        String taskId = "task12345678901234567890";
        AsyncExportManager.ExportTask task = new AsyncExportManager.ExportTask(taskId, "workflow123", "user123", "workflowwithprocesscodehistory");
        task.setStatus(AsyncExportManager.ExportStatus.COMPLETED);
        task.setDownloadUrl("download/temp/workflow123.zip");
        task.setCompletedAt(new java.util.Date());

        when(webRequest.getParameter("taskId")).thenReturn(taskId);
        when(asyncExportManager.getExportTask(taskId)).thenReturn(task);

        // When
        String result = geoweaverController.getExportStatus(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("taskId"));
        assertTrue(result.contains("COMPLETED"));
        assertTrue(result.contains("download/temp/workflow123.zip"));
        verify(asyncExportManager).getExportTask(taskId);
    }

    @Test
    @Timeout(10)
    void testGetExportStatusNotFound() {
        // Given
        String taskId = "nonexistent123";

        when(webRequest.getParameter("taskId")).thenReturn(taskId);
        when(asyncExportManager.getExportTask(taskId)).thenReturn(null);

        // When
        String result = geoweaverController.getExportStatus(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("error"));
        assertTrue(result.contains("Task does not exist or has expired"));
        verify(asyncExportManager).getExportTask(taskId);
    }

    @Test
    @Timeout(10)
    void testGetExportStatusWithNullTaskId() {
        // Given
        when(webRequest.getParameter("taskId")).thenReturn(null);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            geoweaverController.getExportStatus(modelMap, webRequest, httpSession, httpServletRequest);
        });
        verify(webRequest).getParameter("taskId");
    }

    @Test
    @Timeout(10)
    void testGetUserExportTasks() {
        // Given
        ConcurrentHashMap<String, AsyncExportManager.ExportTask> userTasks = new ConcurrentHashMap<>();
        AsyncExportManager.ExportTask task1 = new AsyncExportManager.ExportTask("task1", "workflow1", "user123", "option1");
        AsyncExportManager.ExportTask task2 = new AsyncExportManager.ExportTask("task2", "workflow2", "user123", "option2");
        userTasks.put("task1", task1);
        userTasks.put("task2", task2);

        when(httpSession.getId()).thenReturn("session123");
        when(userTool.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
        when(userTool.getAuthUserId("session123", "127.0.0.1")).thenReturn("user123");
        when(asyncExportManager.getUserExportTasks("user123")).thenReturn(userTasks);

        // When
        String result = geoweaverController.getUserExportTasks(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("task1"));
        assertTrue(result.contains("task2"));
        assertTrue(result.contains("workflow1"));
        assertTrue(result.contains("workflow2"));
        verify(asyncExportManager).getUserExportTasks("user123");
    }

    @Test
    @Timeout(10)
    void testGetUserExportTasksEmpty() {
        // Given
        ConcurrentHashMap<String, AsyncExportManager.ExportTask> emptyTasks = new ConcurrentHashMap<>();

        when(httpSession.getId()).thenReturn("session123");
        when(userTool.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
        when(userTool.getAuthUserId("session123", "127.0.0.1")).thenReturn("user123");
        when(asyncExportManager.getUserExportTasks("user123")).thenReturn(emptyTasks);

        // When
        String result = geoweaverController.getUserExportTasks(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertEquals("[]", result);
        verify(asyncExportManager).getUserExportTasks("user123");
    }

    @Test
    @Timeout(10)
    void testGetUserExportTasksWithException() {
        // Given
        when(httpSession.getId()).thenReturn("session123");
        when(userTool.getClientIp(httpServletRequest)).thenReturn("127.0.0.1");
        when(userTool.getAuthUserId("session123", "127.0.0.1")).thenReturn("user123");
        when(asyncExportManager.getUserExportTasks("user123"))
            .thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            geoweaverController.getUserExportTasks(modelMap, webRequest, httpSession, httpServletRequest);
        });
    }

    @Test
    @Timeout(10)
    void testExportStatusWithFailedTask() {
        // Given
        String taskId = "task12345678901234567890";
        AsyncExportManager.ExportTask task = new AsyncExportManager.ExportTask(taskId, "workflow123", "user123", "workflowwithprocesscodehistory");
        task.setStatus(AsyncExportManager.ExportStatus.FAILED);
        task.setErrorMessage("Export failed due to insufficient disk space");
        task.setCompletedAt(new java.util.Date());

        when(webRequest.getParameter("taskId")).thenReturn(taskId);
        when(asyncExportManager.getExportTask(taskId)).thenReturn(task);

        // When
        String result = geoweaverController.getExportStatus(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("taskId"));
        assertTrue(result.contains("FAILED"));
        assertTrue(result.contains("Export failed due to insufficient disk space"));
        verify(asyncExportManager).getExportTask(taskId);
    }

    @Test
    @Timeout(10)
    void testExportStatusWithProcessingTask() {
        // Given
        String taskId = "task12345678901234567890";
        AsyncExportManager.ExportTask task = new AsyncExportManager.ExportTask(taskId, "workflow123", "user123", "workflowwithprocesscodehistory");
        task.setStatus(AsyncExportManager.ExportStatus.PROCESSING);

        when(webRequest.getParameter("taskId")).thenReturn(taskId);
        when(asyncExportManager.getExportTask(taskId)).thenReturn(task);

        // When
        String result = geoweaverController.getExportStatus(modelMap, webRequest, httpSession, httpServletRequest);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("taskId"));
        assertTrue(result.contains("PROCESSING"));
        assertTrue(result.contains("downloadUrl"));
        assertTrue(result.contains("errorMessage"));
        verify(asyncExportManager).getExportTask(taskId);
    }
}
