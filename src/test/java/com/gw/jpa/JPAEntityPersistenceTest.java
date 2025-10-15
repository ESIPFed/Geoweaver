package com.gw.jpa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.UUID;
import java.util.HashSet;
import java.util.Set;

/**
 * 测试JPA实体的持久化操作和生命周期
 */
public class JPAEntityPersistenceTest {

    private Date testDate;
    private Date testDate2;

    @BeforeEach
    void setUp() {
        testDate = new Date();
        testDate2 = new Date(System.currentTimeMillis() + 1000);
    }

    // ========== 测试实体创建和初始化 ==========
    @Test
    void testEntityCreation() {
        // 测试所有实体都能正确创建
        assertNotNull(new GWUser());
        assertNotNull(new GWProcess());
        assertNotNull(new Workflow());
        assertNotNull(new Host());
        assertNotNull(new Environment());
        assertNotNull(new History());
        assertNotNull(new LogActivity());
        assertNotNull(new Checkpoint());
        assertNotNull(new HistoryDTO("id", testDate, testDate2, "notes", "process", "host", "indicator"));
    }

    // ========== 测试GWUser实体生命周期 ==========
    @Test
    void testGWUserLifecycle() {
        GWUser user = new GWUser();
        
        // 初始状态
        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getRole());
        assertNull(user.getEmail());
        assertNull(user.getIsactive());
        assertNull(user.getRegistration_date());
        assertNull(user.getLast_login_date());
        assertNull(user.getLoggedIn());
        
        // 设置值
        user.setId("user123");
        user.setUsername("testuser");
        user.setPassword("password123");
        user.setRole("admin");
        user.setEmail("test@example.com");
        user.setIsactive(true);
        user.setRegistration_date(testDate);
        user.setLast_login_date(testDate2);
        user.setLoggedIn(true);
        
        // 验证值
        assertEquals("user123", user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("admin", user.getRole());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.getIsactive());
        assertEquals(testDate, user.getRegistration_date());
        assertEquals(testDate2, user.getLast_login_date());
        assertTrue(user.getLoggedIn());
        
        // 修改值
        user.setUsername("newuser");
        user.setPassword("newpassword");
        user.setRole("user");
        user.setEmail("new@example.com");
        user.setIsactive(false);
        user.setLoggedIn(false);
        
        // 验证修改
        assertEquals("newuser", user.getUsername());
        assertEquals("newpassword", user.getPassword());
        assertEquals("user", user.getRole());
        assertEquals("new@example.com", user.getEmail());
        assertFalse(user.getIsactive());
        assertFalse(user.getLoggedIn());
    }

    // ========== 测试GWProcess实体生命周期 ==========
    @Test
    void testGWProcessLifecycle() {
        GWProcess process = new GWProcess();
        
        // 初始状态
        assertNull(process.getId());
        assertNull(process.getName());
        assertNull(process.getDescription());
        assertNull(process.getCode());
        assertNull(process.getLang());
        assertNull(process.getOwner());
        assertNull(process.getConfidential());
        
        // 设置值
        process.setId("process123");
        process.setName("Test Process");
        process.setDescription("Test description");
        process.setCode("print('Hello World')");
        process.setLang("python");
        process.setOwner("user123");
        process.setConfidential("true");
        
        // 验证值
        assertEquals("process123", process.getId());
        assertEquals("Test Process", process.getName());
        assertEquals("Test description", process.getDescription());
        assertEquals("print('Hello World')", process.getCode());
        assertEquals("python", process.getLang());
        assertEquals("user123", process.getOwner());
        assertEquals("true", process.getConfidential());
    }

    // ========== 测试Workflow实体生命周期 ==========
    @Test
    void testWorkflowLifecycle() {
        Workflow workflow = new Workflow();
        
        // 初始状态
        assertNull(workflow.getId());
        assertNull(workflow.getName());
        assertNull(workflow.getDescription());
        assertNull(workflow.getOwner());
        assertNull(workflow.getConfidential());
        assertNull(workflow.getEdges());
        assertNull(workflow.getNodes());
        
        // 设置值
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");
        workflow.setDescription("Test workflow description");
        workflow.setOwner("user123");
        workflow.setConfidential("false");
        workflow.setEdges("edge1,edge2,edge3");
        workflow.setNodes("node1,node2,node3");
        
        // 验证值
        assertEquals("workflow123", workflow.getId());
        assertEquals("Test Workflow", workflow.getName());
        assertEquals("Test workflow description", workflow.getDescription());
        assertEquals("user123", workflow.getOwner());
        assertEquals("false", workflow.getConfidential());
        assertEquals("edge1,edge2,edge3", workflow.getEdges());
        assertEquals("node1,node2,node3", workflow.getNodes());
    }

    // ========== 测试Host实体生命周期 ==========
    @Test
    void testHostLifecycle() {
        Host host = new Host();
        
        // 初始状态
        assertNull(host.getId());
        assertNull(host.getName());
        assertNull(host.getIp());
        assertNull(host.getPort());
        assertNull(host.getUsername());
        assertNull(host.getOwner());
        assertNull(host.getType());
        assertNull(host.getUrl());
        assertNull(host.getConfidential());
        assertNull(host.getEnvs());
        
        // 设置值
        host.setId("host123");
        host.setName("Test Host");
        host.setIp("192.168.1.1");
        host.setPort("22");
        host.setUsername("admin");
        host.setOwner("user123");
        host.setType("ssh");
        host.setUrl("ssh://192.168.1.1");
        host.setConfidential("true");
        
        // 验证值
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

    // ========== 测试Environment实体生命周期 ==========
    @Test
    void testEnvironmentLifecycle() {
        Environment environment = new Environment();
        
        // 初始状态
        assertNull(environment.getId());
        assertNull(environment.getName());
        assertNull(environment.getType());
        assertNull(environment.getBin());
        assertNull(environment.getPyenv());
        assertNull(environment.getBasedir());
        assertNull(environment.getSettings());
        assertNull(environment.getHostobj());
        
        // 设置值
        environment.setId("env123");
        environment.setName("Python Environment");
        environment.setType("python");
        environment.setBin("/usr/bin/python");
        environment.setPyenv("python3.8");
        environment.setBasedir("/home/user");
        environment.setSettings("{\"version\": \"3.8\"}");
        
        // 验证值
        assertEquals("env123", environment.getId());
        assertEquals("Python Environment", environment.getName());
        assertEquals("python", environment.getType());
        assertEquals("/usr/bin/python", environment.getBin());
        assertEquals("python3.8", environment.getPyenv());
        assertEquals("/home/user", environment.getBasedir());
        assertEquals("{\"version\": \"3.8\"}", environment.getSettings());
    }

    // ========== 测试History实体生命周期 ==========
    @Test
    void testHistoryLifecycle() {
        History history = new History();
        
        // 初始状态
        assertNull(history.getHistory_id());
        assertNull(history.getHistory_input());
        assertNull(history.getHistory_output());
        assertNull(history.getHistory_begin_time());
        assertNull(history.getHistory_end_time());
        assertNull(history.getHistory_notes());
        assertNull(history.getHistory_process());
        assertNull(history.getHost_id());
        assertNull(history.getIndicator());
        
        // 设置值
        history.setHistory_id("history123");
        history.setHistory_input("input data");
        history.setHistory_output("output data");
        history.setHistory_begin_time(testDate);
        history.setHistory_end_time(testDate2);
        history.setHistory_notes("Test notes");
        history.setHistory_process("process123");
        history.setHost_id("host123");
        history.setIndicator("success");
        
        // 验证值
        assertEquals("history123", history.getHistory_id());
        assertEquals("input data", history.getHistory_input());
        assertEquals("output data", history.getHistory_output());
        assertEquals(testDate, history.getHistory_begin_time());
        assertEquals(testDate2, history.getHistory_end_time());
        assertEquals("Test notes", history.getHistory_notes());
        assertEquals("process123", history.getHistory_process());
        assertEquals("host123", history.getHost_id());
        assertEquals("success", history.getIndicator());
    }

    // ========== 测试LogActivity实体生命周期 ==========
    @Test
    void testLogActivityLifecycle() {
        LogActivity logActivity = new LogActivity();
        
        // 初始状态
        assertNull(logActivity.getId());
        assertNull(logActivity.getOperator());
        assertNull(logActivity.getCategory());
        assertNull(logActivity.getObjectid());
        assertNull(logActivity.getObjname());
        assertNull(logActivity.getOperation());
        
        // 设置值
        logActivity.setId("log123");
        logActivity.setOperator("user123");
        logActivity.setCategory("process");
        logActivity.setObjectid("process123");
        logActivity.setObjname("Test Process");
        logActivity.setOperation("create");
        
        // 验证值
        assertEquals("log123", logActivity.getId());
        assertEquals("user123", logActivity.getOperator());
        assertEquals("process", logActivity.getCategory());
        assertEquals("process123", logActivity.getObjectid());
        assertEquals("Test Process", logActivity.getObjname());
        assertEquals("create", logActivity.getOperation());
    }

    // ========== 测试Checkpoint实体生命周期 ==========
    @Test
    void testCheckpointLifecycle() {
        Checkpoint checkpoint = new Checkpoint();
        
        // 初始状态
        assertNull(checkpoint.getId());
        assertNull(checkpoint.getExecutionId());
        assertNull(checkpoint.getEdges());
        assertNull(checkpoint.getNodes());
        assertNull(checkpoint.getWorkflow());
        assertNull(checkpoint.getCreatedAt());
        
        // 设置值
        checkpoint.setExecutionId("exec123");
        checkpoint.setEdges("edge1,edge2");
        checkpoint.setNodes("node1,node2");
        checkpoint.setCreatedAt(testDate);
        
        // 验证值
        assertEquals("exec123", checkpoint.getExecutionId());
        assertEquals("edge1,edge2", checkpoint.getEdges());
        assertEquals("node1,node2", checkpoint.getNodes());
        assertEquals(testDate, checkpoint.getCreatedAt());
    }

    // ========== 测试实体关系 ==========
    @Test
    void testEntityRelationships() {
        // 创建Host
        Host host = new Host();
        host.setId("host123");
        host.setName("Test Host");
        
        // 创建Environment并关联到Host
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Python Environment");
        environment.setHostobj(host);
        
        // 验证关系
        assertEquals(host, environment.getHostobj());
        assertEquals("host123", environment.getHostobj().getId());
        
        // 创建Workflow
        Workflow workflow = new Workflow();
        workflow.setId("workflow123");
        workflow.setName("Test Workflow");
        
        // 创建Checkpoint并关联到Workflow
        Checkpoint checkpoint = new Checkpoint();
        checkpoint.setExecutionId("exec123");
        checkpoint.setWorkflow(workflow);
        
        // 验证关系
        assertEquals(workflow, checkpoint.getWorkflow());
        assertEquals("workflow123", checkpoint.getWorkflow().getId());
    }

    // ========== 测试集合关系 ==========
    @Test
    void testCollectionRelationships() {
        // 创建Host
        Host host = new Host();
        host.setId("host123");
        host.setName("Test Host");
        
        // 创建多个Environment
        Environment env1 = new Environment();
        env1.setId("env1");
        env1.setName("Python 3.8");
        env1.setHostobj(host);
        
        Environment env2 = new Environment();
        env2.setId("env2");
        env2.setName("Python 3.9");
        env2.setHostobj(host);
        
        // 创建Environment集合
        Set<Environment> environments = new HashSet<>();
        environments.add(env1);
        environments.add(env2);
        
        // 关联到Host
        host.setEnvs(environments);
        
        // 验证关系
        assertEquals(2, host.getEnvs().size());
        assertTrue(host.getEnvs().contains(env1));
        assertTrue(host.getEnvs().contains(env2));
    }

    // ========== 测试HistoryDTO构造函数 ==========
    @Test
    void testHistoryDTOConstructor() {
        HistoryDTO dto = new HistoryDTO("dto123", testDate, testDate2, "notes", "process", "host", "indicator");
        
        // 验证构造函数设置的值
        assertEquals("dto123", dto.getHistory_id());
        assertEquals(testDate, dto.getHistory_begin_time());
        assertEquals(testDate2, dto.getHistory_end_time());
        assertEquals("notes", dto.getHistory_notes());
        assertEquals("process", dto.getHistory_process());
        assertEquals("host", dto.getHost_id());
        assertEquals("indicator", dto.getIndicator());
    }

    // ========== 测试ExecutionStatus常量 ==========
    @Test
    void testExecutionStatusConstants() {
        // 验证所有常量值
        assertEquals("Done", ExecutionStatus.DONE);
        assertEquals("Failed", ExecutionStatus.FAILED);
        assertEquals("Running", ExecutionStatus.RUNNING);
        assertEquals("Unknown", ExecutionStatus.UNKOWN);
        assertEquals("Stopped", ExecutionStatus.STOPPED);
        assertEquals("Ready", ExecutionStatus.READY);
        assertEquals("Skipped", ExecutionStatus.SKIPPED);
        
        // 验证常量不为null
        assertNotNull(ExecutionStatus.DONE);
        assertNotNull(ExecutionStatus.FAILED);
        assertNotNull(ExecutionStatus.RUNNING);
        assertNotNull(ExecutionStatus.UNKOWN);
        assertNotNull(ExecutionStatus.STOPPED);
        assertNotNull(ExecutionStatus.READY);
        assertNotNull(ExecutionStatus.SKIPPED);
    }

    // ========== 测试边界情况 ==========
    @Test
    void testBoundaryConditions() {
        // 测试空字符串
        GWUser user = new GWUser();
        user.setUsername("");
        user.setPassword("");
        user.setRole("");
        user.setEmail("");
        
        assertEquals("", user.getUsername());
        assertEquals("", user.getPassword());
        assertEquals("", user.getRole());
        assertEquals("", user.getEmail());
        
        // 测试null值 - 注意username和password有@NonNull注解，所以不能设置为null
        // 只测试可以为null的字段
        user.setRole(null);
        user.setEmail(null);
        
        assertNull(user.getRole());
        assertNull(user.getEmail());
    }

    // ========== 测试特殊字符 ==========
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

    // ========== 测试Unicode字符 ==========
    @Test
    void testUnicodeCharacters() {
        Workflow workflow = new Workflow();
        String unicodeString = "测试中文 🚀 émojis 特殊字符";
        
        workflow.setName(unicodeString);
        workflow.setDescription(unicodeString);
        
        assertEquals(unicodeString, workflow.getName());
        assertEquals(unicodeString, workflow.getDescription());
    }

    // ========== 测试大对象字段 ==========
    @Test
    void testLargeObjectFields() {
        // 测试大字符串
        String largeString = "A".repeat(10000);
        
        GWProcess process = new GWProcess();
        process.setDescription(largeString);
        process.setCode(largeString);
        
        assertEquals(largeString, process.getDescription());
        assertEquals(largeString, process.getCode());
        
        Workflow workflow = new Workflow();
        workflow.setEdges(largeString);
        workflow.setNodes(largeString);
        
        assertEquals(largeString, workflow.getEdges());
        assertEquals(largeString, workflow.getNodes());
    }

    // ========== 测试对象相等性 ==========
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
        
        // 但是内容相同
        assertEquals(user1.getId(), user2.getId());
        assertEquals(user1.getUsername(), user2.getUsername());
    }
}
