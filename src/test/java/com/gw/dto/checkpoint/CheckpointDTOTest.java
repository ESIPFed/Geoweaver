package com.gw.dto.checkpoint;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.UUID;

public class CheckpointDTOTest {

    @Test
    void testCheckpointDTO() {
        CheckpointDTO dto = new CheckpointDTO();
        assertNotNull(dto);
        
        // Test ID
        UUID testId = UUID.randomUUID();
        dto.setId(testId);
        assertEquals(testId, dto.getId());
        
        // Test edges
        String testEdges = "[]";
        dto.setEdges(testEdges);
        assertEquals(testEdges, dto.getEdges());
        
        // Test nodes
        String testNodes = "[{\"id\":\"node1\",\"type\":\"process\"}]";
        dto.setNodes(testNodes);
        assertEquals(testNodes, dto.getNodes());
        
        // Test createdAt
        Date testDate = new Date();
        dto.setCreatedAt(testDate);
        assertEquals(testDate, dto.getCreatedAt());
    }

    @Test
    void testCheckpointDTOWithNullValues() {
        CheckpointDTO dto = new CheckpointDTO();
        assertNotNull(dto);
        
        // Test with null values
        assertNull(dto.getId());
        assertNull(dto.getEdges());
        assertNull(dto.getNodes());
        assertNull(dto.getCreatedAt());
        
        // Set null values explicitly
        dto.setId(null);
        dto.setEdges(null);
        dto.setNodes(null);
        dto.setCreatedAt(null);
        
        assertNull(dto.getId());
        assertNull(dto.getEdges());
        assertNull(dto.getNodes());
        assertNull(dto.getCreatedAt());
    }

    @Test
    void testCheckpointDTOWithComplexData() {
        CheckpointDTO dto = new CheckpointDTO();
        
        // Test with complex JSON data
        String complexNodes = "[{\"id\":\"node1\",\"type\":\"process\",\"name\":\"Test Process\",\"position\":{\"x\":100,\"y\":200}}]";
        String complexEdges = "[{\"id\":\"edge1\",\"source\":\"node1\",\"target\":\"node2\",\"type\":\"default\"}]";
        
        dto.setNodes(complexNodes);
        dto.setEdges(complexEdges);
        
        assertEquals(complexNodes, dto.getNodes());
        assertEquals(complexEdges, dto.getEdges());
    }

    @Test
    void testCheckpointCreateRequest() {
        CheckpointCreateRequest request = new CheckpointCreateRequest();
        assertNotNull(request);
        
        // Test workflowId
        String workflowId = "workflow-123";
        request.setWorkflowId(workflowId);
        assertEquals(workflowId, request.getWorkflowId());
        
        // Test executionId
        String executionId = "execution-456";
        request.setExecutionId(executionId);
        assertEquals(executionId, request.getExecutionId());
    }

    @Test
    void testCheckpointCreateRequestWithNullValues() {
        CheckpointCreateRequest request = new CheckpointCreateRequest();
        assertNotNull(request);
        
        // Test with null values
        assertNull(request.getWorkflowId());
        assertNull(request.getExecutionId());
        
        // Set null values explicitly
        request.setWorkflowId(null);
        request.setExecutionId(null);
        
        assertNull(request.getWorkflowId());
        assertNull(request.getExecutionId());
    }

    @Test
    void testCheckpointCreateRequestWithEmptyStrings() {
        CheckpointCreateRequest request = new CheckpointCreateRequest();
        
        // Test with empty strings
        request.setWorkflowId("");
        request.setExecutionId("");
        
        assertEquals("", request.getWorkflowId());
        assertEquals("", request.getExecutionId());
    }

    @Test
    void testCheckpointRestoreDTO() {
        CheckpointRestoreDTO dto = new CheckpointRestoreDTO();
        assertNotNull(dto);
        
        // Test workflowId
        String workflowId = "workflow-restore-123";
        dto.setWorkflowId(workflowId);
        assertEquals(workflowId, dto.getWorkflowId());
        
        // Test executionId
        String executionId = "execution-restore-456";
        dto.setExecutionId(executionId);
        assertEquals(executionId, dto.getExecutionId());
    }

    @Test
    void testCheckpointRestoreDTOWithNullValues() {
        CheckpointRestoreDTO dto = new CheckpointRestoreDTO();
        assertNotNull(dto);
        
        // Test with null values
        assertNull(dto.getWorkflowId());
        assertNull(dto.getExecutionId());
        
        // Set null values explicitly
        dto.setWorkflowId(null);
        dto.setExecutionId(null);
        
        assertNull(dto.getWorkflowId());
        assertNull(dto.getExecutionId());
    }

    @Test
    void testCheckpointRestoreDTOWithEmptyStrings() {
        CheckpointRestoreDTO dto = new CheckpointRestoreDTO();
        
        // Test with empty strings
        dto.setWorkflowId("");
        dto.setExecutionId("");
        
        assertEquals("", dto.getWorkflowId());
        assertEquals("", dto.getExecutionId());
    }

    @Test
    void testCheckpointDTOGettersAndSetters() {
        CheckpointDTO dto = new CheckpointDTO();
        
        // Test all getters and setters
        UUID id = UUID.randomUUID();
        String edges = "test-edges";
        String nodes = "test-nodes";
        Date createdAt = new Date();
        
        dto.setId(id);
        dto.setEdges(edges);
        dto.setNodes(nodes);
        dto.setCreatedAt(createdAt);
        
        assertEquals(id, dto.getId());
        assertEquals(edges, dto.getEdges());
        assertEquals(nodes, dto.getNodes());
        assertEquals(createdAt, dto.getCreatedAt());
    }

    @Test
    void testCheckpointCreateRequestGettersAndSetters() {
        CheckpointCreateRequest request = new CheckpointCreateRequest();
        
        // Test all getters and setters
        String workflowId = "test-workflow-id";
        String executionId = "test-execution-id";
        
        request.setWorkflowId(workflowId);
        request.setExecutionId(executionId);
        
        assertEquals(workflowId, request.getWorkflowId());
        assertEquals(executionId, request.getExecutionId());
    }

    @Test
    void testCheckpointRestoreDTOGettersAndSetters() {
        CheckpointRestoreDTO dto = new CheckpointRestoreDTO();
        
        // Test all getters and setters
        String workflowId = "restore-workflow-id";
        String executionId = "restore-execution-id";
        
        dto.setWorkflowId(workflowId);
        dto.setExecutionId(executionId);
        
        assertEquals(workflowId, dto.getWorkflowId());
        assertEquals(executionId, dto.getExecutionId());
    }

    @Test
    void testCheckpointDTOWithSpecialCharacters() {
        CheckpointDTO dto = new CheckpointDTO();
        
        // Test with special characters in strings
        String specialEdges = "[\"edge1\",\"edge2\",\"edge3\"]";
        String specialNodes = "[{\"id\":\"node-1\",\"name\":\"Test Node & More\",\"type\":\"process\"}]";
        
        dto.setEdges(specialEdges);
        dto.setNodes(specialNodes);
        
        assertEquals(specialEdges, dto.getEdges());
        assertEquals(specialNodes, dto.getNodes());
    }

    @Test
    void testCheckpointCreateRequestWithSpecialCharacters() {
        CheckpointCreateRequest request = new CheckpointCreateRequest();
        
        // Test with special characters
        String workflowId = "workflow-123_456";
        String executionId = "execution@domain.com";
        
        request.setWorkflowId(workflowId);
        request.setExecutionId(executionId);
        
        assertEquals(workflowId, request.getWorkflowId());
        assertEquals(executionId, request.getExecutionId());
    }

    @Test
    void testCheckpointRestoreDTOWithSpecialCharacters() {
        CheckpointRestoreDTO dto = new CheckpointRestoreDTO();
        
        // Test with special characters
        String workflowId = "restore-workflow-123_456";
        String executionId = "restore@domain.com";
        
        dto.setWorkflowId(workflowId);
        dto.setExecutionId(executionId);
        
        assertEquals(workflowId, dto.getWorkflowId());
        assertEquals(executionId, dto.getExecutionId());
    }
}
