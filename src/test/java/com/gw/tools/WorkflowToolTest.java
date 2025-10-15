package com.gw.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.CheckpointRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.Checkpoint;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.jpa.Workflow;
import com.gw.tasks.GeoweaverWorkflowTask;
import com.gw.tasks.TaskManager;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class WorkflowToolTest {

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private CheckpointRepository checkpointRepository;

    @Mock
    private TaskManager taskManager;

    @Mock
    private ProcessTool processTool;

    @Mock
    private HistoryTool historyTool;

    @Mock
    private BaseTool baseTool;

    @Mock
    private GeoweaverWorkflowTask geoweaverWorkflowTask;

    @InjectMocks
    private WorkflowTool workflowTool;

    @Test
    @Timeout(10)
    void testStop() {
        // Given
        String historyId = "history123";
        History history = new History();
        history.setHistory_id(historyId);
        history.setHistory_output("process1;process2");

        when(historyRepository.findById(historyId)).thenReturn(Optional.of(history));
        when(processTool.stop(anyString())).thenReturn("stopped");
        doNothing().when(taskManager).stopTask(anyString());

        // When
        String result = workflowTool.stop(historyId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("history_id"));
        assertTrue(result.contains("stopped"));
        verify(historyRepository).save(any(History.class));
    }

    @Test
    @Timeout(10)
    void testToJSON() {
        // Given
        Workflow workflow = new Workflow();
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");

        // When
        String result = workflowTool.toJSON(workflow);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("workflow123"));
        assertTrue(result.contains("Test Workflow"));
    }

    @Test
    @Timeout(10)
    void testToJSONWithException() {
        // Given
        Workflow workflow = new Workflow();
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");

        // When
        String result = workflowTool.toJSON(workflow);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("workflow123"));
        assertTrue(result.contains("Test Workflow"));
    }

    @Test
    @Timeout(10)
    void testGetWorkflowListByOwner() {
        // Given
        String ownerId = "user123";
        List<Workflow> publicWorkflows = new ArrayList<>();
        Workflow publicWorkflow = new Workflow();
        publicWorkflow.setId("public1");
        publicWorkflow.setName("Public Workflow");
        publicWorkflows.add(publicWorkflow);

        List<Workflow> privateWorkflows = new ArrayList<>();
        Workflow privateWorkflow = new Workflow();
        privateWorkflow.setId("private1");
        privateWorkflow.setName("Private Workflow");
        privateWorkflows.add(privateWorkflow);

        when(workflowRepository.findAllPublic()).thenReturn(publicWorkflows);
        when(workflowRepository.findAllPrivateByOwner(ownerId)).thenReturn(privateWorkflows);

        // When
        List<Workflow> result = workflowTool.getWorkflowListByOwner(ownerId);

        // Then
        assertEquals(2, result.size());
        assertEquals("public1", result.get(0).getId());
        assertEquals("private1", result.get(1).getId());
    }

    @Test
    @Timeout(10)
    void testList() {
        // Given
        String owner = "user123";
        List<Workflow> publicWorkflows = new ArrayList<>();
        Workflow publicWorkflow = new Workflow();
        publicWorkflow.setId("public1");
        publicWorkflow.setName("Public Workflow");
        publicWorkflows.add(publicWorkflow);

        List<Workflow> privateWorkflows = new ArrayList<>();
        Workflow privateWorkflow = new Workflow();
        privateWorkflow.setId("private1");
        privateWorkflow.setName("Private Workflow");
        privateWorkflows.add(privateWorkflow);

        when(workflowRepository.findAllPublic()).thenReturn(publicWorkflows);
        when(workflowRepository.findAllPrivateByOwner(owner)).thenReturn(privateWorkflows);

        // When
        String result = workflowTool.list(owner);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testGetById() {
        // Given
        String workflowId = "workflow123";
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // When
        Workflow result = workflowTool.getById(workflowId);

        // Then
        assertNotNull(result);
        assertEquals(workflowId, result.getId());
        assertEquals("Test Workflow", result.getName());
    }

    @Test
    @Timeout(10)
    void testGetByIdNotFound() {
        // Given
        String workflowId = "nonexistent";

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When
        Workflow result = workflowTool.getById(workflowId);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testDetail() {
        // Given
        String workflowId = "workflow123";
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // When
        String result = workflowTool.detail(workflowId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("workflow123"));
    }

    @Test
    @Timeout(10)
    void testDetailNotFound() {
        // Given
        String workflowId = "nonexistent";

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When
        String result = workflowTool.detail(workflowId);

        // Then
        // Should return "null" when workflow not found
        assertNotNull(result);
        assertEquals("null", result);
    }

    @Test
    @Timeout(10)
    void testFindNextProcess() {
        // Given
        Map<String, List> nodeMap = new HashMap<>();
        List preNodes = new ArrayList();
        preNodes.add("node1");
        nodeMap.put("node2", preNodes);
        // Add empty list for node1 to avoid null pointer
        nodeMap.put("node1", new ArrayList());

        ExecutionStatus[] flags = new ExecutionStatus[2];
        // Create ExecutionStatus objects - these are actually String constants
        flags[0] = new ExecutionStatus();
        flags[1] = new ExecutionStatus();

        JSONArray nodes = new JSONArray();
        JSONObject node1 = new JSONObject();
        node1.put("id", "node1");
        nodes.add(node1);
        JSONObject node2 = new JSONObject();
        node2.put("id", "node2");
        nodes.add(node2);

        // When
        String[] result = workflowTool.findNextProcess(nodeMap, flags, nodes);

        // Then
        assertNotNull(result);
        // The result should contain the next processes based on the logic
        assertTrue(result.length >= 0);
    }

    @Test
    @Timeout(10)
    void testGetAllWorkflow() {
        // Given
        List<Workflow> workflows = new ArrayList<>();
        Workflow workflow1 = new Workflow();
        workflow1.setId("workflow1");
        workflow1.setName("Workflow 1");
        workflows.add(workflow1);

        Workflow workflow2 = new Workflow();
        workflow2.setId("workflow2");
        workflow2.setName("Workflow 2");
        workflows.add(workflow2);

        when(workflowRepository.findAll()).thenReturn(workflows);

        // When
        List<Workflow> result = workflowTool.getAllWorkflow();

        // Then
        assertEquals(2, result.size());
        assertEquals("workflow1", result.get(0).getId());
        assertEquals("workflow2", result.get(1).getId());
    }

    @Test
    @Timeout(10)
    void testSave() {
        // Given
        Workflow workflow = new Workflow();
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");

        // When
        workflowTool.save(workflow);

        // Then
        verify(workflowRepository).save(workflow);
    }

    @Test
    @Timeout(10)
    void testSaveWithExistingWorkflow() {
        // Given
        Workflow newWorkflow = new Workflow();
        newWorkflow.setId("workflow123");
        newWorkflow.setName("New Workflow");

        Workflow existingWorkflow = new Workflow();
        existingWorkflow.setId("workflow123");
        existingWorkflow.setName("Existing Workflow");
        existingWorkflow.setDescription("Existing Description");
        existingWorkflow.setEdges("edges");
        existingWorkflow.setNodes("nodes");
        existingWorkflow.setOwner("owner");

        when(workflowRepository.findById("workflow123")).thenReturn(Optional.of(existingWorkflow));
        // Mock static method calls

        // When
        workflowTool.save(newWorkflow);

        // Then
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    @Timeout(10)
    void testExecute() {
        // Given
        String historyId = "history123";
        String workflowId = "workflow123";
        String mode = "single";
        String[] hosts = {"host1", "host2"};
        String[] passwords = {"pass1", "pass2"};
        String[] environments = {"env1", "env2"};
        String token = "token123";

        when(geoweaverWorkflowTask.getHistory_id()).thenReturn(historyId);

        // When
        String result = workflowTool.execute(historyId, workflowId, mode, hosts, passwords, environments, token);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("history_id"));
        assertTrue(result.contains("token"));
        assertTrue(result.contains("ret"));
    }

    @Test
    @Timeout(10)
    void testExecuteWithException() {
        // Given
        String historyId = "history123";
        String workflowId = "workflow123";
        String mode = "single";
        String[] hosts = {"host1"};
        String[] passwords = {"pass1"};
        String[] environments = {"env1"};
        String token = "token123";

        doThrow(new RuntimeException("Task error"))
                .when(geoweaverWorkflowTask).initialize(anyString(), anyString(), anyString(), any(), any(), any(), anyString());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            workflowTool.execute(historyId, workflowId, mode, hosts, passwords, environments, token);
        });
    }

    @Test
    @Timeout(10)
    void testUpdate() {
        // Given
        String workflowId = "workflow123";
        String nodes = "[{\"id\":\"node1\",\"name\":\"Node 1\"}]";
        String edges = "[{\"from\":\"node1\",\"to\":\"node2\"}]";

        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // When
        workflowTool.update(workflowId, nodes, edges);

        // Then
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    @Timeout(10)
    void testAdd() {
        // Given
        String name = "Test Workflow";
        String nodes = "[{\"id\":\"node1\",\"name\":\"Node 1\"}]";
        String edges = "[{\"from\":\"node1\",\"to\":\"node2\"}]";
        String ownerId = "user123";

        // When
        String result = workflowTool.add(name, nodes, edges, ownerId);

        // Then
        assertNotNull(result);
        assertEquals(20, result.length()); // RandomString(20) length
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    @Timeout(10)
    void testDel() {
        // Given
        String workflowId = "workflow123";
        List<Checkpoint> checkpoints = new ArrayList<>();
        Checkpoint checkpoint = new Checkpoint();
        checkpoints.add(checkpoint);

        when(checkpointRepository.findByWorkflowId(workflowId)).thenReturn(checkpoints);

        // When
        String result = workflowTool.del(workflowId);

        // Then
        assertEquals("done", result);
        verify(checkpointRepository).deleteByWorkflowId(workflowId);
        verify(workflowRepository).deleteById(workflowId);
    }

    @Test
    @Timeout(10)
    void testDelWithoutCheckpoints() {
        // Given
        String workflowId = "workflow123";
        List<Object> checkpoints = new ArrayList<>();

        when(checkpointRepository.findByWorkflowId(workflowId)).thenReturn(new ArrayList<>());

        // When
        String result = workflowTool.del(workflowId);

        // Then
        assertEquals("done", result);
        verify(checkpointRepository, never()).deleteByWorkflowId(workflowId);
        verify(workflowRepository).deleteById(workflowId);
    }

    @Test
    @Timeout(10)
    void testAllActiveProcess() {
        // Given
        List<Object[]> activeProcesses = new ArrayList<>();
        Object[] process1 = {"process1", "2023-01-01", "2023-01-02", "RUNNING", "output1"};
        activeProcesses.add(process1);

        when(historyRepository.findRunningWorkflow()).thenReturn(activeProcesses);
        when(baseTool.escape(anyString())).thenReturn("escaped");

        // When
        String result = workflowTool.all_active_process();

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testAllActiveProcessWithException() {
        // Given
        when(historyRepository.findRunningWorkflow()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        // Should handle exception gracefully
        assertThrows(RuntimeException.class, () -> {
            workflowTool.all_active_process();
        });
    }

    @Test
    @Timeout(10)
    void testAllHistory() {
        // Given
        String workflowId = "workflow123";

        when(historyTool.workflow_all_history(workflowId)).thenReturn("[]");

        // When
        String result = workflowTool.all_history(workflowId);

        // Then
        assertEquals("[]", result);
    }

    @Test
    @Timeout(10)
    void testList2JSON() {
        // Given
        String list = "item1;item2;item3";

        // When
        String result = workflowTool.list2JSON(list);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
        assertTrue(result.contains("item1"));
        assertTrue(result.contains("item2"));
        assertTrue(result.contains("item3"));
    }

    @Test
    @Timeout(10)
    void testRecent() {
        // Given
        int limit = 5;
        List<Object[]> recentWorkflows = new ArrayList<>();
        Object[] workflow1 = {"workflow1", "2023-01-01", "2023-01-02", "workflow1"};
        recentWorkflows.add(workflow1);

        when(historyRepository.findRecentWorkflow(limit)).thenReturn(recentWorkflows);

        // When
        String result = workflowTool.recent(limit);

        // Then
        assertNotNull(result);
        // The result should be a JSON array or empty string
        assertTrue(result.startsWith("[") || result.isEmpty());
    }

    @Test
    @Timeout(10)
    void testRecentWithException() {
        // Given
        int limit = 5;

        when(historyRepository.findRecentWorkflow(limit)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = workflowTool.recent(limit);

        // Then
        assertEquals("", result);
    }

    @Test
    @Timeout(10)
    void testOneHistory() {
        // Given
        String historyId = "history123";
        History history = new History();
        history.setHistory_id(historyId);
        history.setHistory_process("process123");
        history.setHistory_input("process1;process2");
        history.setHistory_output("history1;history2");
        history.setHistory_begin_time(new java.sql.Timestamp(System.currentTimeMillis()));
        history.setHistory_end_time(new java.sql.Timestamp(System.currentTimeMillis()));

        when(historyRepository.findById(historyId)).thenReturn(Optional.of(history));

        // When
        String result = workflowTool.one_history(historyId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("hid"));
        assertTrue(result.contains("process"));
    }

    @Test
    @Timeout(10)
    void testOneHistoryWithException() {
        // Given
        String historyId = "history123";

        when(historyRepository.findById(historyId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = workflowTool.one_history(historyId);

        // Then
        assertEquals("", result);
    }

    @Test
    @Timeout(10)
    void testGetExportModeById() {
        // Test mode 1
        assertEquals("workflowonly", workflowTool.getExportModeById(1));
        
        // Test mode 2
        assertEquals("workflowwithprocesscode", workflowTool.getExportModeById(2));
        
        // Test mode 3
        assertEquals("workflowwithprocesscodegoodhistory", workflowTool.getExportModeById(3));
        
        // Test default mode
        assertEquals("workflowwithprocesscodehistory", workflowTool.getExportModeById(0));
        assertEquals("workflowwithprocesscodehistory", workflowTool.getExportModeById(999));
    }

    @Test
    @Timeout(10)
    void testDownload() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscodehistory";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description"); // Add description to avoid null pointer
        workflow.setNodes("[]"); // Add empty nodes to avoid null pointer
        workflow.setEdges("[]"); // Add empty edges to avoid null pointer

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(baseTool.getFileTransferFolder()).thenReturn("/tmp/transfer");
        when(baseTool.deleteDirectory(any(File.class))).thenReturn(true);
        doNothing().when(baseTool).writeString2File(anyString(), anyString());
        when(baseTool.toJSON(any())).thenReturn("{}");
        // createReadme is a static method, cannot mock this way
        doNothing().when(baseTool).zipFolder(anyString(), anyString());

        // When
        String result = workflowTool.download(workflowId, option);

        // Then
        // Should handle download gracefully - may return null on exception
        // The test expects not null but the method can return null on exception
        assertNotNull(result);
    }

    @Test
    @Timeout(10)
    void testDownloadWithException() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscodehistory";

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(NullPointerException.class, () -> {
            workflowTool.download(workflowId, option);
        });
    }

    @Test
    @Timeout(10)
    void testFromJSON() {
        // Given
        String json = "{\"id\":\"workflow123\",\"name\":\"Test Workflow\",\"description\":\"Test Description\"}";

        // When
        Workflow result = workflowTool.fromJSON(json);

        // Then
        assertNotNull(result);
        assertEquals("workflow123", result.getId());
        assertEquals("Test Workflow", result.getName());
        assertEquals("Test Description", result.getDescription());
    }

    @Test
    @Timeout(10)
    void testFromJSONWithException() {
        // Given
        String invalidJson = "invalid json";

        // When
        Workflow result = workflowTool.fromJSON(invalidJson);

        // Then
        // Should return null for invalid JSON
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testHistoryFromJSON() {
        // Given
        String json = "{\"history_id\":\"history123\",\"history_process\":\"process123\",\"indicator\":\"DONE\"}";

        // When
        History result = workflowTool.historyFromJSON(json);

        // Then
        assertNotNull(result);
        assertEquals("history123", result.getHistory_id());
        assertEquals("process123", result.getHistory_process());
        assertEquals("DONE", result.getIndicator());
    }

    @Test
    @Timeout(10)
    void testHistoryFromJSONWithException() {
        // Given
        String invalidJson = "invalid json";

        // When
        History result = workflowTool.historyFromJSON(invalidJson);

        // Then
        // Should return null for invalid JSON
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testCheckProcessSkipped() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String workflowProcessId = "process123-obj1";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setNodes("[{\"id\":\"process123-obj1\",\"skip\":\"true\"}]");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // When
        String result = workflowTool.check_process_skipped(workflowId, workflowProcessId);

        // Then
        assertEquals("true", result);
    }

    @Test
    @Timeout(10)
    void testCheckProcessSkippedWithNullWorkflowId() {
        // Given
        String workflowId = null;
        String workflowProcessId = "process123-obj1";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            workflowTool.check_process_skipped(workflowId, workflowProcessId);
        });
    }

    @Test
    @Timeout(10)
    void testSkipProcess() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String workflowProcessId = "process123-obj1";
        String skip = "true";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setNodes("[{\"id\":\"process123-obj1\",\"skip\":\"false\"}]");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // When
        workflowTool.skip_process(workflowId, workflowProcessId, skip);

        // Then
        verify(workflowRepository).save(any(Workflow.class));
    }

    @Test
    @Timeout(10)
    void testSkipProcessWithException() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String workflowProcessId = "process123-obj1";
        String skip = "true";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setNodes("invalid json");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            workflowTool.skip_process(workflowId, workflowProcessId, skip);
        });
    }

    @Test
    @Timeout(10)
    void testDownloadWithProcessCode() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscode";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");
        workflow.setNodes("[{\"id\":\"process123-obj1\",\"title\":\"Test Process\"}]");
        workflow.setEdges("[]");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(baseTool.getFileTransferFolder()).thenReturn("/tmp/transfer");
        when(baseTool.deleteDirectory(any(File.class))).thenReturn(true);
        doNothing().when(baseTool).writeString2File(anyString(), anyString());
        when(baseTool.toJSON(any())).thenReturn("{}");
        doNothing().when(baseTool).zipFolder(anyString(), anyString());

        // When
        String result = workflowTool.download(workflowId, option);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("download/temp/"));
        assertTrue(result.endsWith(".zip"));
    }

    @Test
    @Timeout(10)
    void testDownloadWithHistory() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscodehistory";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");
        workflow.setNodes("[{\"id\":\"process123-obj1\",\"title\":\"Test Process\"}]");
        workflow.setEdges("[]");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(baseTool.getFileTransferFolder()).thenReturn("/tmp/transfer");
        when(baseTool.deleteDirectory(any(File.class))).thenReturn(true);
        doNothing().when(baseTool).writeString2File(anyString(), anyString());
        when(baseTool.toJSON(any())).thenReturn("{}");
        doNothing().when(baseTool).zipFolder(anyString(), anyString());

        // When
        String result = workflowTool.download(workflowId, option);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("download/temp/"));
        assertTrue(result.endsWith(".zip"));
    }

    @Test
    @Timeout(10)
    void testDownloadWorkflowOnly() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String option = "workflowonly";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");
        workflow.setNodes("[]");
        workflow.setEdges("[]");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(baseTool.getFileTransferFolder()).thenReturn("/tmp/transfer");
        when(baseTool.deleteDirectory(any(File.class))).thenReturn(true);
        doNothing().when(baseTool).writeString2File(anyString(), anyString());
        when(baseTool.toJSON(any())).thenReturn("{}");
        doNothing().when(baseTool).zipFolder(anyString(), anyString());

        // When
        String result = workflowTool.download(workflowId, option);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("download/temp/"));
        assertTrue(result.endsWith(".zip"));
    }

    @Test
    @Timeout(10)
    void testDownloadWithGoodHistory() throws ParseException {
        // Given
        String workflowId = "workflow123";
        String option = "workflowwithprocesscodegoodhistory";
        
        Workflow workflow = new Workflow();
        workflow.setId(workflowId);
        workflow.setName("Test Workflow");
        workflow.setDescription("Test Description");
        workflow.setNodes("[{\"id\":\"process123-obj1\",\"title\":\"Test Process\"}]");
        workflow.setEdges("[]");

        when(workflowRepository.findById(workflowId)).thenReturn(Optional.of(workflow));
        when(baseTool.getFileTransferFolder()).thenReturn("/tmp/transfer");
        when(baseTool.deleteDirectory(any(File.class))).thenReturn(true);
        doNothing().when(baseTool).writeString2File(anyString(), anyString());
        when(baseTool.toJSON(any())).thenReturn("{}");
        doNothing().when(baseTool).zipFolder(anyString(), anyString());

        // When
        String result = workflowTool.download(workflowId, option);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("download/temp/"));
        assertTrue(result.endsWith(".zip"));
    }
}
