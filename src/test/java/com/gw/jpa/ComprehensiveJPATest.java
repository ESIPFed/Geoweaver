package com.gw.jpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * 全面的JPA实体测试类，旨在达到80%的测试覆盖率
 */
public class ComprehensiveJPATest {

    private Date testDate;
    private Date testDate2;

    @BeforeEach
    void setUp() {
        testDate = new Date();
        testDate2 = new Date(System.currentTimeMillis() + 1000);
    }

    // ========== GWUser 实体测试 ==========
    @Test
    void testGWUserAllFields() {
        GWUser user = new GWUser();
        
        // 测试所有字段的setter和getter
        user.setId("user123");
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRole("admin");
        user.setEmail("test@example.com");
        user.setIsactive(true);
        user.setRegistration_date(testDate);
        user.setLast_login_date(testDate2);
        user.setLoggedIn(true);

        // 验证所有字段
        assertEquals("user123", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("admin", user.getRole());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getIsactive());
        assertEquals(testDate, user.getRegistration_date());
        assertEquals(testDate2, user.getLast_login_date());
        assertTrue(user.getLoggedIn());
    }

    @Test
    void testGWUserNullValues() {
        GWUser user = new GWUser();
        
        // 测试null值处理
        user.setRole(null);
        user.setEmail(null);
        user.setIsactive(null);
        user.setRegistration_date(null);
        user.setLast_login_date(null);
        user.setLoggedIn(null);

        assertNull(user.getRole());
        assertNull(user.getEmail());
        assertNull(user.getIsactive());
        assertNull(user.getRegistration_date());
        assertNull(user.getLast_login_date());
        assertNull(user.getLoggedIn());
    }

    // ========== GWProcess 实体测试 ==========
    @Test
    void testGWProcessAllFields() {
        GWProcess process = new GWProcess();
        
        process.setId("process123");
        process.setName("Test Process");
        process.setDescription("This is a test process description");
        process.setCode("print('Hello World')");
        process.setLang("python");
        process.setOwner("user123");
        process.setConfidential("true");

        assertEquals("process123", process.getId());
        assertEquals("Test Process", process.getName());
        assertEquals("This is a test process description", process.getDescription());
        assertEquals("print('Hello World')", process.getCode());
        assertEquals("python", process.getLang());
        assertEquals("user123", process.getOwner());
        assertEquals("true", process.getConfidential());
    }

    @Test
    void testGWProcessLobFields() {
        GWProcess process = new GWProcess();
        
        // 测试大对象字段
        String longDescription = "A".repeat(1000);
        String longCode = "B".repeat(2000);
        
        process.setDescription(longDescription);
        process.setCode(longCode);

        assertEquals(longDescription, process.getDescription());
        assertEquals(longCode, process.getCode());
    }

    // ========== Workflow 实体测试 ==========
    @Test
    void testWorkflowAllFields() {
        Workflow workflow = new Workflow();
        
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");
        workflow.setDescription("Test workflow description");
        workflow.setOwner("user123");
        workflow.setConfidential("false");
        workflow.setEdges("edge1,edge2,edge3");
        workflow.setNodes("node1,node2,node3");

        assertEquals("workflow123", workflow.getId());
        assertEquals("Test Workflow", workflow.getName());
        assertEquals("Test workflow description", workflow.getDescription());
        assertEquals("user123", workflow.getOwner());
        assertEquals("false", workflow.getConfidential());
        assertEquals("edge1,edge2,edge3", workflow.getEdges());
        assertEquals("node1,node2,node3", workflow.getNodes());
    }

    @Test
    void testWorkflowLobFields() {
        Workflow workflow = new Workflow();
        
        String longEdges = "edge".repeat(100);
        String longNodes = "node".repeat(100);
        
        workflow.setEdges(longEdges);
        workflow.setNodes(longNodes);

        assertEquals(longEdges, workflow.getEdges());
        assertEquals(longNodes, workflow.getNodes());
    }

    // ========== Host 实体测试 ==========
    @Test
    void testHostAllFields() {
        Host host = new Host();
        
        host.setId("host123");
        host.setName("Test Host");
        host.setIp("192.168.1.1");
        host.setPort("22");
        host.setUsername("admin");
        host.setOwner("user123");
        host.setType("ssh");
        host.setUrl("ssh://192.168.1.1");
        host.setConfidential("true");

        assertEquals("host123", host.getId());
        assertEquals("Test Host", host.getName());
        assertEquals("192.168.1.1", host.getIp());
        assertEquals("22", host.getPort());
        assertEquals("admin", host.getUsername());
        assertEquals("user123", host.getOwner());
        assertEquals("ssh", host.getType());
        assertEquals("ssh://192.168.1.1", host.getUrl());
        assertEquals("true", host.getConfidential());
    }

    @Test
    void testHostEnvironmentRelationship() {
        Host host = new Host();
        host.setId("host123");
        
        Environment env1 = new Environment();
        env1.setId("env1");
        env1.setName("Python 3.8");
        env1.setHostobj(host);
        
        Environment env2 = new Environment();
        env2.setId("env2");
        env2.setName("Python 3.9");
        env2.setHostobj(host);
        
        Set<Environment> environments = new HashSet<>();
        environments.add(env1);
        environments.add(env2);
        
        host.setEnvs(environments);
        
        assertEquals(2, host.getEnvs().size());
        assertTrue(host.getEnvs().contains(env1));
        assertTrue(host.getEnvs().contains(env2));
    }

    // ========== Environment 实体测试 ==========
    @Test
    void testEnvironmentAllFields() {
        Environment environment = new Environment();
        
        environment.setId("env123");
        environment.setName("Python Environment");
        environment.setType("python");
        environment.setBin("/usr/bin/python");
        environment.setPyenv("python3.8");
        environment.setBasedir("/home/user");
        environment.setSettings("{\"version\": \"3.8\", \"packages\": [\"numpy\", \"pandas\"]}");

        assertEquals("env123", environment.getId());
        assertEquals("Python Environment", environment.getName());
        assertEquals("python", environment.getType());
        assertEquals("/usr/bin/python", environment.getBin());
        assertEquals("python3.8", environment.getPyenv());
        assertEquals("/home/user", environment.getBasedir());
        assertEquals("{\"version\": \"3.8\", \"packages\": [\"numpy\", \"pandas\"]}", environment.getSettings());
    }

    @Test
    void testEnvironmentHostRelationship() {
        Host host = new Host();
        host.setId("host123");
        host.setName("Test Host");
        
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");
        environment.setHostobj(host);

        assertEquals(host, environment.getHostobj());
        assertEquals("host123", environment.getHostobj().getId());
    }

    // ========== History 实体测试 ==========
    @Test
    void testHistoryAllFields() {
        History history = new History();
        
        history.setHistory_id("history123");
        history.setHistory_input("input data");
        history.setHistory_output("output data");
        history.setHistory_begin_time(testDate);
        history.setHistory_end_time(testDate2);
        history.setHistory_notes("Test execution notes");
        history.setHistory_process("process123");
        history.setHost_id("host123");
        history.setIndicator("success");

        assertEquals("history123", history.getHistory_id());
        assertEquals("input data", history.getHistory_input());
        assertEquals("output data", history.getHistory_output());
        assertEquals(testDate, history.getHistory_begin_time());
        assertEquals(testDate2, history.getHistory_end_time());
        assertEquals("Test execution notes", history.getHistory_notes());
        assertEquals("process123", history.getHistory_process());
        assertEquals("host123", history.getHost_id());
        assertEquals("success", history.getIndicator());
    }

    @Test
    void testHistoryLobFields() {
        History history = new History();
        
        String longInput = "input".repeat(100);
        String longOutput = "output".repeat(100);
        String longNotes = "notes".repeat(100);
        
        history.setHistory_input(longInput);
        history.setHistory_output(longOutput);
        history.setHistory_notes(longNotes);

        assertEquals(longInput, history.getHistory_input());
        assertEquals(longOutput, history.getHistory_output());
        assertEquals(longNotes, history.getHistory_notes());
    }

    // ========== LogActivity 实体测试 ==========
    @Test
    void testLogActivityAllFields() {
        LogActivity logActivity = new LogActivity();
        
        logActivity.setId("log123");
        logActivity.setOperator("user123");
        logActivity.setCategory("process");
        logActivity.setObjectid("process123");
        logActivity.setObjname("Test Process");
        logActivity.setOperation("create");

        assertEquals("log123", logActivity.getId());
        assertEquals("user123", logActivity.getOperator());
        assertEquals("process", logActivity.getCategory());
        assertEquals("process123", logActivity.getObjectid());
        assertEquals("Test Process", logActivity.getObjname());
        assertEquals("create", logActivity.getOperation());
    }

    // ========== Checkpoint 实体测试 ==========
    @Test
    void testCheckpointAllFields() {
        Checkpoint checkpoint = new Checkpoint();
        
        checkpoint.setExecutionId("exec123");
        checkpoint.setEdges("edge1,edge2");
        checkpoint.setNodes("node1,node2");
        checkpoint.setCreatedAt(testDate);

        assertEquals("exec123", checkpoint.getExecutionId());
        assertEquals("edge1,edge2", checkpoint.getEdges());
        assertEquals("node1,node2", checkpoint.getNodes());
        assertEquals(testDate, checkpoint.getCreatedAt());
    }

    @Test
    void testCheckpointWorkflowRelationship() {
        Workflow workflow = new Workflow();
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");
        
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setExecutionId("exec123");
        checkpoint.setWorkflow(workflow);

        assertEquals(workflow, checkpoint.getWorkflow());
        assertEquals("workflow123", checkpoint.getWorkflow().getId());
    }

    @Test
    void testCheckpointLobFields() {
        Checkpoint checkpoint = new Checkpoint();
        
        String longEdges = "edge".repeat(100);
        String longNodes = "node".repeat(100);
        
        checkpoint.setEdges(longEdges);
        checkpoint.setNodes(longNodes);

        assertEquals(longEdges, checkpoint.getEdges());
        assertEquals(longNodes, checkpoint.getNodes());
    }

    // ========== HistoryDTO 测试 ==========
    @Test
    void testHistoryDTOConstructor() {
        HistoryDTO dto = new HistoryDTO("dto123", testDate, testDate2, "notes", "process", "host", "indicator");
        
        assertEquals("dto123", dto.getHistory_id());
        assertEquals(testDate, dto.getHistory_begin_time());
        assertEquals(testDate2, dto.getHistory_end_time());
        assertEquals("notes", dto.getHistory_notes());
        assertEquals("process", dto.getHistory_process());
        assertEquals("host", dto.getHost_id());
        assertEquals("indicator", dto.getIndicator());
    }

    @Test
    void testHistoryDTOSetters() {
        HistoryDTO dto = new HistoryDTO("dto123", testDate, testDate2, "notes", "process", "host", "indicator");
        
        // 测试setter方法
        dto.setHistory_id("new_dto123");
        dto.setHistory_begin_time(testDate2);
        dto.setHistory_end_time(testDate);
        dto.setHistory_notes("new notes");
        dto.setHistory_process("new process");
        dto.setHost_id("new host");
        dto.setIndicator("new indicator");

        assertEquals("new_dto123", dto.getHistory_id());
        assertEquals(testDate2, dto.getHistory_begin_time());
        assertEquals(testDate, dto.getHistory_end_time());
        assertEquals("new notes", dto.getHistory_notes());
        assertEquals("new process", dto.getHistory_process());
        assertEquals("new host", dto.getHost_id());
        assertEquals("new indicator", dto.getIndicator());
    }

    // ========== ExecutionStatus 测试 ==========
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

    // ========== 边界值和异常情况测试 ==========
    @Test
    void testEmptyStrings() {
        GWUser user = new GWUser();
        user.setUsername("");
        user.setPassword("");
        user.setRole("");
        user.setEmail("");
        
        assertEquals("", user.getUsername());
        assertEquals("", user.getPassword());
        assertEquals("", user.getRole());
        assertEquals("", user.getEmail());
    }

    @Test
    void testSpecialCharacters() {
        GWProcess process = new GWProcess();
        String specialChars = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        
        process.setName(specialChars);
        process.setDescription(specialChars);
        process.setCode(specialChars);
        
        assertEquals(specialChars, process.getName());
        assertEquals(specialChars, process.getDescription());
        assertEquals(specialChars, process.getCode());
    }

    @Test
    void testUnicodeCharacters() {
        Workflow workflow = new Workflow();
        String unicodeString = "测试中文 🚀 émojis 特殊字符";
        
        workflow.setName(unicodeString);
        workflow.setDescription(unicodeString);
        
        assertEquals(unicodeString, workflow.getName());
        assertEquals(unicodeString, workflow.getDescription());
    }

    // ========== 对象相等性测试 ==========
    @Test
    void testObjectEquality() {
        GWUser user1 = new GWUser();
        user1.setId("user123");
        user1.setUsername("testuser");
        
        GWUser user2 = new GWUser();
        user2.setId("user123");
        user2.setUsername("testuser");
        
        // 由于没有重写equals方法，这些对象不相等
        assertNotEquals(user1, user2);
        assertNotSame(user1, user2);
    }

    // ========== 集合操作测试 ==========
    @Test
    void testHostEnvironmentCollection() {
        Host host = new Host();
        host.setId("host123");
        
        Set<Environment> environments = new HashSet<>();
        
        Environment env1 = new Environment();
        env1.setId("env1");
        env1.setName("Python 3.8");
        
        Environment env2 = new Environment();
        env2.setId("env2");
        env2.setName("Python 3.9");
        
        environments.add(env1);
        environments.add(env2);
        
        host.setEnvs(environments);
        
        assertEquals(2, host.getEnvs().size());
        assertTrue(host.getEnvs().contains(env1));
        assertTrue(host.getEnvs().contains(env2));
    }

    @Test
    void testEmptyCollections() {
        Host host = new Host();
        host.setEnvs(new HashSet<>());
        
        assertNotNull(host.getEnvs());
        assertTrue(host.getEnvs().isEmpty());
    }
}
