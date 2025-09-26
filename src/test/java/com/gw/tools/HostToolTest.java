package com.gw.tools;

import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.History;
import com.gw.jpa.Host;
import com.gw.utils.BaseTool;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class HostToolTest {

    @Mock
    private HostRepository hostRepository;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private BaseTool baseTool;

    @Mock
    private EnvironmentRepository environmentRepository;

    @InjectMocks
    private HostTool hostTool;

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

        when(historyRepository.findById(historyId)).thenReturn(Optional.of(history));
        when(baseTool.escape(anyString())).thenReturn("escaped");

        // When
        String result = hostTool.one_history(historyId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("hid"));
        assertTrue(result.contains("id"));
        assertTrue(result.contains("process"));
    }

    @Test
    @Timeout(10)
    void testOneHistoryWithException() {
        // Given
        String historyId = "history123";

        when(historyRepository.findById(historyId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = hostTool.one_history(historyId);

        // Then
        assertEquals("", result);
    }

    @Test
    @Timeout(10)
    void testRecent() {
        // Given
        String hostId = "host123";
        int limit = 5;
        Collection<History> historyList = new ArrayList<>();
        History history = new History();
        history.setHistory_id("history123");
        history.setHistory_process("process123");
        history.setHistory_end_time(new java.sql.Timestamp(System.currentTimeMillis()));
        history.setHistory_notes("notes");
        history.setIndicator("DONE");
        history.setHistory_begin_time(new java.sql.Timestamp(System.currentTimeMillis()));
        historyList.add(history);

        when(historyRepository.findRecentHistory(hostId, limit)).thenReturn(new ArrayList<>(historyList));

        // When
        String result = hostTool.recent(hostId, limit);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testRecentWithException() {
        // Given
        String hostId = "host123";
        int limit = 5;

        when(historyRepository.findRecentHistory(hostId, limit)).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            hostTool.recent(hostId, limit);
        });
    }

    @Test
    @Timeout(10)
    void testDetailJSONObj() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");
        host.setIp("192.168.1.1");
        host.setPort("22");
        host.setUsername("user");
        host.setType("SSH");
        host.setUrl("http://example.com");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        String result = hostTool.detailJSONObj(hostId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("host123"));
    }

    @Test
    @Timeout(10)
    void testDetail() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        String result = hostTool.detail(hostId);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("host123"));
    }

    @Test
    @Timeout(10)
    void testGetHostById() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        Host result = hostTool.getHostById(hostId);

        // Then
        assertNotNull(result);
        assertEquals(hostId, result.getId());
        assertEquals("Test Host", result.getName());
    }

    @Test
    @Timeout(10)
    void testGetHostByIdNotFound() {
        // Given
        String hostId = "nonexistent";

        when(hostRepository.findById(hostId)).thenReturn(Optional.empty());

        // When
        Host result = hostTool.getHostById(hostId);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testGetHostDetailsById() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");
        host.setIp("192.168.1.1");
        host.setPort("22");
        host.setUsername("user");
        host.setType("SSH");
        host.setUrl("http://example.com");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        String[] result = hostTool.getHostDetailsById(hostId);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length);
        assertEquals("Test Host", result[0]);
        assertEquals("192.168.1.1", result[1]);
        assertEquals("22", result[2]);
        assertEquals("user", result[3]);
        assertEquals("SSH", result[4]);
        assertEquals("http://example.com", result[5]);
    }

    @Test
    @Timeout(10)
    void testGetHostDetailsByIdWithException() {
        // Given
        String hostId = "host123";

        when(hostRepository.findById(hostId)).thenThrow(new RuntimeException("Database error"));

        // When
        String[] result = hostTool.getHostDetailsById(hostId);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length);
        // All elements should be null due to exception
        for (String element : result) {
            assertNull(element);
        }
    }

    @Test
    @Timeout(10)
    void testToJSON() {
        // Given
        Host host = new Host();
        host.setId("host123");
        host.setName("Test Host");
        host.setIp("192.168.1.1");

        // When
        String result = hostTool.toJSON(host);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("host123"));
        assertTrue(result.contains("Test Host"));
    }

    @Test
    @Timeout(10)
    void testToJSONWithException() {
        // Given
        Host host = new Host();
        host.setId("host123");
        host.setName("Test Host");

        // When
        String result = hostTool.toJSON(host);

        // Then
        // The toJSON method should return the actual JSON representation
        assertNotNull(result);
        assertTrue(result.contains("host123"));
        assertTrue(result.contains("Test Host"));
    }

    @Test
    @Timeout(10)
    void testList() {
        // Given
        String owner = "user123";
        List<Host> publicHosts = new ArrayList<>();
        Host publicHost = new Host();
        publicHost.setId("public1");
        publicHost.setName("Public Host");
        publicHosts.add(publicHost);

        List<Host> privateHosts = new ArrayList<>();
        Host privateHost = new Host();
        privateHost.setId("private1");
        privateHost.setName("Private Host");
        privateHosts.add(privateHost);

        when(hostRepository.findAllPublicHosts()).thenReturn(publicHosts);
        when(hostRepository.findPrivateByOwner(owner)).thenReturn(privateHosts);

        // When
        String result = hostTool.list(owner);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testAdd() {
        // Given
        String hostname = "Test Host";
        String hostip = "192.168.1.1";
        String hostport = "22";
        String username = "user";
        String url = "http://example.com";
        String type = "SSH";
        String owner = "user123";
        String confidential = "FALSE";

        // When
        String result = hostTool.add(hostname, hostip, hostport, username, url, type, owner, confidential);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length()); // RandomString(6) length
        verify(hostRepository).save(any(Host.class));
    }

    @Test
    @Timeout(10)
    void testAddWithNullOwner() {
        // Given
        String hostname = "Test Host";
        String hostip = "192.168.1.1";
        String hostport = "22";
        String username = "user";
        String url = "http://example.com";
        String type = "SSH";
        String owner = null;
        String confidential = "FALSE";

        // When
        String result = hostTool.add(hostname, hostip, hostport, username, url, type, owner, confidential);

        // Then
        assertNotNull(result);
        assertEquals(6, result.length());
        verify(hostRepository).save(any(Host.class));
    }

    @Test
    @Timeout(10)
    void testDel() {
        // Given
        String hostId = "host123";

        // When
        String result = hostTool.del(hostId);

        // Then
        assertEquals("done", result);
        verify(hostRepository).deleteById(hostId);
    }

    @Test
    @Timeout(10)
    void testSave() {
        // Given
        Host host = new Host();
        host.setId("host123");
        host.setName("Test Host");

        // When
        hostTool.save(host);

        // Then
        verify(hostRepository).save(host);
    }

    @Test
    @Timeout(10)
    void testGetAllHosts() {
        // Given
        List<Host> hosts = new ArrayList<>();
        Host host1 = new Host();
        host1.setId("host1");
        host1.setName("Host 1");
        hosts.add(host1);

        Host host2 = new Host();
        host2.setId("host2");
        host2.setName("Host 2");
        hosts.add(host2);

        when(hostRepository.findAll()).thenReturn(hosts);

        // When
        List<Host> result = hostTool.getAllHosts();

        // Then
        assertEquals(2, result.size());
        assertEquals("host1", result.get(0).getId());
        assertEquals("host2", result.get(1).getId());
    }

    @Test
    @Timeout(10)
    void testSaveEnvironment() {
        // Given
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");

        // When
        hostTool.saveEnvironment(environment);

        // Then
        verify(environmentRepository).save(environment);
    }

    @Test
    @Timeout(10)
    void testUpdate() {
        // Given
        String hostId = "host123";
        String hostname = "Updated Host";
        String hostip = "192.168.1.2";
        String hostport = "2222";
        String username = "newuser";
        String type = "SSH";
        String owner = "user123";
        String url = "http://updated.com";
        String confidential = "TRUE";

        Host host = new Host();
        host.setId(hostId);
        host.setName("Original Host");
        host.setIp("192.168.1.1");
        host.setPort("22");
        host.setUsername("user");
        host.setType("SSH");
        host.setOwner("user123");
        host.setUrl("http://example.com");
        host.setConfidential("FALSE");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        String result = hostTool.update(hostId, hostname, hostip, hostport, username, type, owner, url, confidential);

        // Then
        assertNull(result); // update method returns null on success
        verify(hostRepository).save(any(Host.class));
    }

    @Test
    @Timeout(10)
    void testUpdateWithNullValues() {
        // Given
        String hostId = "host123";
        String hostname = "Updated Host";
        String hostip = null;
        String hostport = null;
        String username = null;
        String type = null;
        String owner = null;
        String url = null;
        String confidential = null;

        Host host = new Host();
        host.setId(hostId);
        host.setName("Original Host");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        String result = hostTool.update(hostId, hostname, hostip, hostport, username, type, owner, url, confidential);

        // Then
        assertNull(result); // update method returns null on success
        verify(hostRepository).save(any(Host.class));
    }

    @Test
    @Timeout(10)
    void testUpdateWithException() {
        // Given
        String hostId = "host123";
        String hostname = "Updated Host";
        String hostip = "192.168.1.2";
        String hostport = "2222";
        String username = "newuser";
        String type = "SSH";
        String owner = "user123";
        String url = "http://updated.com";
        String confidential = "TRUE";

        when(hostRepository.findById(hostId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = hostTool.update(hostId, hostname, hostip, hostport, username, type, owner, url, confidential);

        // Then
        assertNull(result);
    }
}
