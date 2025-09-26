package com.gw.jpa;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.UUID;

public class SimpleJPATest {

    @Test
    void testHostEntity() {
        Host host = new Host();
        assertNotNull(host);
        host.setId("host1");
        assertEquals("host1", host.getId());
    }

    @Test
    void testGWProcessEntity() {
        GWProcess process = new GWProcess();
        assertNotNull(process);
        process.setId("process1");
        assertEquals("process1", process.getId());
    }

    @Test
    void testWorkflowEntity() {
        Workflow workflow = new Workflow();
        assertNotNull(workflow);
        workflow.setId("workflow1");
        assertEquals("workflow1", workflow.getId());
    }

    @Test
    void testHistoryEntity() {
        History history = new History();
        assertNotNull(history);
        history.setHistory_id("history1");
        assertEquals("history1", history.getHistory_id());
    }

    @Test
    void testCheckpointEntity() {
        Checkpoint checkpoint = new Checkpoint();
        assertNotNull(checkpoint);
        // Checkpoint uses UUID, so we can't set a simple string ID
        // The ID is null initially until persisted
        assertNull(checkpoint.getId()); // Should be null initially
    }

    @Test
    void testEnvironmentEntity() {
        Environment environment = new Environment();
        assertNotNull(environment);
        environment.setId("env1");
        assertEquals("env1", environment.getId());
    }

    @Test
    void testGWUserEntity() {
        GWUser user = new GWUser();
        assertNotNull(user);
        user.setId("user1");
        assertEquals("user1", user.getId());
    }

    @Test
    void testLogActivityEntity() {
        LogActivity logActivity = new LogActivity();
        assertNotNull(logActivity);
        logActivity.setId("log1");
        assertEquals("log1", logActivity.getId());
    }

    @Test
    void testHistoryDTO() {
        // HistoryDTO has a constructor with arguments, so we need to provide them
        Date now = new Date();
        HistoryDTO historyDTO = new HistoryDTO("dto1", now, now, "notes", "process", "host", "indicator");
        assertNotNull(historyDTO);
        assertEquals("dto1", historyDTO.getHistory_id());
    }

    @Test
    void testExecutionStatusConstants() {
        assertEquals("Done", ExecutionStatus.DONE);
        assertEquals("Failed", ExecutionStatus.FAILED);
        assertEquals("Running", ExecutionStatus.RUNNING);
        assertEquals("Unknown", ExecutionStatus.UNKOWN);
        assertEquals("Stopped", ExecutionStatus.STOPPED);
        assertEquals("Ready", ExecutionStatus.READY);
        assertEquals("Skipped", ExecutionStatus.SKIPPED);
    }
}