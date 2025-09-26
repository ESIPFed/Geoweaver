package com.gw.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.HistoryRepository;
import com.gw.database.ProcessRepository;
import com.gw.jpa.ExecutionStatus;
import com.gw.jpa.GWProcess;
import com.gw.jpa.History;
import com.gw.local.LocalSession;
import com.gw.ssh.SSHSession;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import com.gw.web.GeoweaverController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@ExtendWith(MockitoExtension.class)
public class ProcessToolTest {

    @Mock
    private HistoryTool historyTool;

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private BaseTool baseTool;

    @Mock
    private GeoweaverController sessionManager;

    @InjectMocks
    private ProcessTool processTool;

    private String workspace = "/tmp/workspace";
    private String uploadFilePath = "/tmp/upload";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(processTool, "workspace", workspace);
        ReflectionTestUtils.setField(processTool, "upload_file_path", uploadFilePath);
    }

    @Test
    @Timeout(10)
    void testSave() {
        // Given
        GWProcess process = new GWProcess();
        process.setId("process123");
        process.setName("Test Process");

        // When
        processTool.save(process);

        // Then
        verify(processRepository).save(process);
    }

    @Test
    @Timeout(10)
    void testGetAllProcesses() {
        // Given
        List<GWProcess> expectedProcesses = new ArrayList<>();
        GWProcess process1 = new GWProcess();
        process1.setId("process1");
        process1.setName("Process 1");
        expectedProcesses.add(process1);

        GWProcess process2 = new GWProcess();
        process2.setId("process2");
        process2.setName("Process 2");
        expectedProcesses.add(process2);

        when(processRepository.findAll()).thenReturn(expectedProcesses);

        // When
        List<GWProcess> result = processTool.getAllProcesses();

        // Then
        assertEquals(2, result.size());
        assertEquals("process1", result.get(0).getId());
        assertEquals("process2", result.get(1).getId());
    }

    @Test
    @Timeout(10)
    void testToJSON() {
        // Given
        GWProcess process = new GWProcess();
        process.setId("process123");
        process.setName("Test Process");
        process.setCode("print('hello')");

        // When
        String json = processTool.toJSON(process);

        // Then
        assertNotNull(json);
        assertTrue(json.contains("process123"));
        assertTrue(json.contains("Test Process"));
    }

    @Test
    @Timeout(10)
    void testToJSONWithException() {
        // Given
        GWProcess process = new GWProcess();
        process.setId("process123");
        process.setName("Test Process");

        // When
        String json = processTool.toJSON(process);

        // Then
        // Should handle JSON conversion gracefully
        assertNotNull(json);
        assertTrue(json.contains("process123") || json.equals("{}"));
    }

    @Test
    @Timeout(10)
    void testList() throws SQLException {
        // Given
        String owner = "user123";
        List<GWProcess> publicProcesses = new ArrayList<>();
        GWProcess publicProcess = new GWProcess();
        publicProcess.setId("public1");
        publicProcess.setName("Public Process");
        publicProcesses.add(publicProcess);

        List<GWProcess> privateProcesses = new ArrayList<>();
        GWProcess privateProcess = new GWProcess();
        privateProcess.setId("private1");
        privateProcess.setName("Private Process");
        privateProcesses.add(privateProcess);

        when(processRepository.findAllPublic()).thenReturn(publicProcesses);
        when(processRepository.findAllPrivateByOwner(owner)).thenReturn(privateProcesses);

        // When
        String result = processTool.list(owner);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testGetProcessById() {
        // Given
        String processId = "process123";
        GWProcess expectedProcess = new GWProcess();
        expectedProcess.setId(processId);
        expectedProcess.setName("Test Process");

        when(processRepository.findById(processId)).thenReturn(Optional.of(expectedProcess));

        // When
        GWProcess result = processTool.getProcessById(processId);

        // Then
        assertNotNull(result);
        assertEquals(processId, result.getId());
        assertEquals("Test Process", result.getName());
    }

    @Test
    @Timeout(10)
    void testEscapeUnsafeCharacters() {
        // Given
        String codeWithUnsafeChars = "bash\\\nimport";

        // When
        String result = processTool.escapeUnsafeCharacters(codeWithUnsafeChars);

        // Then
        assertNotNull(result);
        // The method should handle unsafe characters
        assertTrue(result.length() > 0);
    }

    @Test
    @Timeout(10)
    void testEscapeUnsafeCharactersWithNull() {
        // Given
        // No mocking needed for this test

        // When
        String result = processTool.escapeUnsafeCharacters(null);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testDetail() throws JsonProcessingException {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setName("Test Process");
        process.setCode("print('hello')");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        // Mock static method calls

        // When
        String result = processTool.detail(processId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(processId));
    }

    @Test
    @Timeout(10)
    void testDetailWithUnsafeCharacters() throws JsonProcessingException {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setName("Test Process");
        process.setCode("bash\\\nimport");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        // Mock static method calls
        // Mock unescape method
        // Note: This is a method call, not a mock

        // When
        String result = processTool.detail(processId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains(processId));
    }

    @Test
    @Timeout(10)
    void testGetSuffix() {
        // Test shell suffix
        assertEquals("sh", processTool.getSuffix("shell"));
        
        // Test python suffix
        assertEquals("py", processTool.getSuffix("python"));
        
        // Test builtin suffix
        assertEquals("builtin", processTool.getSuffix("builtin"));
        
        // Test unknown suffix
        assertEquals("", processTool.getSuffix("unknown"));
    }

    @Test
    @Timeout(10)
    void testGetProcessFileName() {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setName("test_script");
        process.setLang("python");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        // When
        String result = processTool.getProcessFileName(processId);

        // Then
        assertEquals("test_script.py", result);
    }

    @Test
    @Timeout(10)
    void testUnescape() {
        // Given
        String escapedCode = "bash-.-import-·-test-··-data->-newline-!-return";
        String expectedCode = "bash-.-import-·-test-··-data->-newline-!-return"; // The unescape method uses StringEscapeUtils.unescapeJson which doesn't change this string

        // When
        String result = processTool.unescape(escapedCode);

        // Then
        assertEquals(expectedCode, result);
    }

    @Test
    @Timeout(10)
    void testUnescapeWithNull() {
        // Given
        // No mocking needed for this test

        // When
        String result = processTool.unescape(null);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testUpdate() {
        // Given
        GWProcess process = new GWProcess();
        process.setId("process123");
        process.setName("Updated Process");

        // When
        processTool.update(process);

        // Then
        verify(processRepository).save(process);
    }

    @Test
    @Timeout(10)
    void testFromJSON() {
        // Given
        String json = "{\"id\":\"process123\",\"name\":\"Test Process\",\"code\":\"print('hello')\"}";

        // When
        GWProcess result = processTool.fromJSON(json);

        // Then
        assertNotNull(result);
        assertEquals("process123", result.getId());
        assertEquals("Test Process", result.getName());
        assertEquals("print('hello')", result.getCode());
    }

    @Test
    @Timeout(10)
    void testFromJSONWithException() {
        // Given
        String invalidJson = "invalid json";

        // When
        GWProcess result = processTool.fromJSON(invalidJson);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testUpdateWithParameters() {
        // Given
        String id = "process123";
        String name = "Updated Process";
        String lang = "python";
        String code = "print('updated')";
        String description = "Updated description";

        GWProcess process = new GWProcess();
        process.setId(id);
        process.setName(name);
        process.setLang(lang);
        process.setCode(code);
        process.setDescription(description);

        when(processRepository.findById(id)).thenReturn(Optional.of(process));
        when(baseTool.escape(code)).thenReturn(code);

        // When
        processTool.update(id, name, lang, code, description);

        // Then
        verify(processRepository).save(any(GWProcess.class));
    }

    @Test
    @Timeout(10)
    void testAddLocal() {
        // Given
        String name = "test_script";
        String lang = "python";
        String code = "print('hello')";
        String desc = "Test description";

        when(baseTool.getFileTransferFolder()).thenReturn("/tmp/transfer");
        doNothing().when(baseTool).writeString2File(anyString(), anyString());

        // When
        String result = processTool.add_local(name, lang, code, desc);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length()); // RandomString(6) length
        verify(processRepository).save(any(GWProcess.class));
    }

    @Test
    @Timeout(10)
    void testAddDatabase() {
        // Given
        String name = "test_script";
        String lang = "python";
        String code = "print('hello')";
        String desc = "Test description";
        String ownerId = "user123";
        String confidential = "FALSE";

        when(baseTool.escape(code)).thenReturn(code);

        // When
        String result = processTool.add_database(name, lang, code, desc, ownerId, confidential);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length()); // RandomString(6) length
        verify(processRepository).save(any(GWProcess.class));
    }

    @Test
    @Timeout(10)
    void testAdd() {
        // Given
        String name = "test_script";
        String lang = "python";
        String code = "print('hello')";
        String desc = "Test description";
        String ownerId = "user123";
        String confidential = "FALSE";

        when(baseTool.escape(code)).thenReturn(code);

        // When
        String result = processTool.add(name, lang, code, desc, ownerId, confidential);

        // Then
        assertNotNull(result);
        verify(processRepository).save(any(GWProcess.class));
    }

    @Test
    @Timeout(10)
    void testDel() {
        // Given
        String processId = "process123";

        // When
        String result = processTool.del(processId);

        // Then
        assertEquals("done", result);
        verify(processRepository).deleteById(processId);
    }

    @Test
    @Timeout(10)
    void testGetNameById() {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setName("Test Process");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        // When
        String result = processTool.getNameById(processId);

        // Then
        assertEquals("Test Process", result);
    }

    @Test
    @Timeout(10)
    void testGetCodeById() {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setCode("print('hello')");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        // When
        String result = processTool.getCodeById(processId);

        // Then
        assertEquals("print('hello')", result);
    }

    @Test
    @Timeout(10)
    void testGetCodeByIdWithUnsafeCharacters() {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setCode("bash\\\nimport");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        // Mock unescape method
        // Note: This is a method call, not a mock

        // When
        String result = processTool.getCodeById(processId);

        // Then
        assertEquals("bash\nimport", result);
    }

    @Test
    @Timeout(10)
    void testGetTypeById() {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setLang("python");
        process.setDescription("Python script");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));
        // Mock static method calls

        // When
        String result = processTool.getTypeById(processId);

        // Then
        assertEquals("python", result);
    }

    @Test
    @Timeout(10)
    void testGetTypeByIdWithNullLang() {
        // Given
        String processId = "process123";
        GWProcess process = new GWProcess();
        process.setId(processId);
        process.setLang(null);
        process.setDescription("Python script");

        when(processRepository.findById(processId)).thenReturn(Optional.of(process));

        // When
        String result = processTool.getTypeById(processId);

        // Then
        assertEquals("Python script", result);
    }

    @Test
    @Timeout(10)
    void testGetTypeByIdWithProcessNotFound() {
        // Given
        String processId = "nonexistent";

        when(processRepository.findById(processId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            processTool.getTypeById(processId);
        });
    }

    @Test
    @Timeout(10)
    void testStop() {
        // Given
        String historyId = "history123";
        SSHSession mockSSHSession = mock(SSHSession.class);
        LocalSession mockLocalSession = mock(LocalSession.class);

        // Mock session manager behavior
        // Note: These fields are not accessible in the test, so we'll skip this test

        // When
        String result = processTool.stop(historyId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("history_id"));
        assertTrue(result.contains("stopped"));
        verify(historyTool).stop(historyId);
    }

    @Test
    @Timeout(10)
    void testStopWithException() {
        // Given
        String historyId = "history123";

        // Mock session manager behavior
        // Note: These fields are not accessible in the test, so we'll skip this test

        // When
        String result = processTool.stop(historyId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("history_id"));
        assertTrue(result.contains("stopped"));
    }

    @Test
    @Timeout(10)
    void testRecent() {
        // Given
        int limit = 5;
        List<Object[]> recentProcesses = new ArrayList<>();
        // The recent method expects 15 elements in the array (index 0-14)
        Object[] process1 = {"process1", "2023-01-01", "2023-01-02", "output1", "DONE", "host1", "user1", "notes1", "status1", "host2", "user2", "notes2", "host3", "user3", "name1"};
        recentProcesses.add(process1);

        when(historyRepository.findRecentProcess(limit)).thenReturn(recentProcesses);

        // When
        String result = processTool.recent(limit);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testRecentWithException() {
        // Given
        int limit = 5;

        when(historyRepository.findRecentProcess(limit)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            processTool.recent(limit);
        });
    }

    @Test
    @Timeout(10)
    void testRemoveClob() {
        // Given
        String clob = "STRINGDECODE('test content')";

        // When
        String result = processTool.removeClob(clob);

        // Then
        assertEquals("test content", result);
    }

    @Test
    @Timeout(10)
    void testRemoveClobWithoutStringDecode() {
        // Given
        String clob = "regular content";

        // When
        String result = processTool.removeClob(clob);

        // Then
        assertEquals("regular content", result);
    }

    @Test
    @Timeout(10)
    void testOneHistory() {
        // Given
        String historyId = "history123";
        History history = new History();
        history.setHistory_id(historyId);
        history.setHistory_process("process123");
        history.setHistory_input("input");
        history.setHistory_output("output");
        history.setHistory_begin_time(new java.sql.Timestamp(System.currentTimeMillis()));
        history.setHistory_end_time(new java.sql.Timestamp(System.currentTimeMillis()));
        history.setHost_id("host123");
        history.setIndicator("DONE");

        GWProcess process = new GWProcess();
        process.setName("Test Process");
        process.setLang("python");
        process.setConfidential("FALSE");

        when(historyRepository.findById(historyId)).thenReturn(Optional.of(history));
        when(processRepository.findById("process123")).thenReturn(Optional.of(process));
        when(baseTool.escape(anyString())).thenReturn("escaped");

        // When
        String result = processTool.one_history(historyId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("hid"));
        assertTrue(result.contains("id"));
        assertTrue(result.contains("name"));
    }

    @Test
    @Timeout(10)
    void testOneHistoryWithException() {
        // Given
        String historyId = "history123";

        when(historyRepository.findById(historyId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = processTool.one_history(historyId);

        // Then
        assertEquals("", result);
    }

    @Test
    @Timeout(10)
    void testAllActiveProcess() {
        // Given
        List<Object[]> activeProcesses = new ArrayList<>();
        // The all_active_process method expects 9 elements in the array (index 0-8)
        Object[] process1 = {"process1", "2023-01-01", "2023-01-02", "output1", "RUNNING", "host1", "user1", "notes1", "status1"};
        activeProcesses.add(process1);

        when(historyRepository.findRunningProcess()).thenReturn(activeProcesses);
        when(baseTool.escape(anyString())).thenReturn("escaped");

        // When
        String result = processTool.all_active_process();

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testAllActiveProcessWithException() {
        // Given
        when(historyRepository.findRunningProcess()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            processTool.all_active_process();
        });
    }

    @Test
    @Timeout(10)
    void testAllHistory() {
        // Given
        String processId = "process123";
        boolean ignoreSkipped = false;
        String mode = "full";

        when(historyTool.process_all_history(processId, ignoreSkipped, mode)).thenReturn("[]");

        // When
        String result = processTool.all_history(processId, ignoreSkipped, mode);

        // Then
        assertEquals("[]", result);
    }

    @Test
    @Timeout(10)
    void testAllHistoryWithDefaultMode() {
        // Given
        String processId = "process123";
        String mode = "default";

        when(historyTool.process_all_history(processId, false, mode)).thenReturn("[]");

        // When
        String result = processTool.all_history(processId, mode);

        // Then
        assertEquals("[]", result);
    }
}
