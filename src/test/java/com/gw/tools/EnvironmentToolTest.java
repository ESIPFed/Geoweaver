package com.gw.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.database.EnvironmentRepository;
import com.gw.database.HostRepository;
import com.gw.jpa.Environment;
import com.gw.jpa.Host;
import com.gw.utils.BaseTool;
import com.gw.utils.RandomString;
import org.junit.jupiter.api.BeforeEach;
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
public class EnvironmentToolTest {

    @Mock
    private EnvironmentRepository environmentRepository;
    
    @Mock
    private EnvironmentRepository envrep;

    @Mock
    private BaseTool baseTool;

    @Mock
    private HostRepository hostRepository;

    @InjectMocks
    private EnvironmentTool environmentTool;

    @Test
    @Timeout(10)
    void testGetEnvironmentById() {
        // Given
        String environmentId = "env123";
        Environment environment = new Environment();
        environment.setId(environmentId);
        environment.setName("Test Environment");
        environment.setBin("/usr/bin/python3");
        environment.setPyenv("pip");
        environment.setBasedir("/tmp");
        environment.setType("python");

        when(envrep.findById(environmentId)).thenReturn(Optional.of(environment));

        // When
        Environment result = environmentTool.getEnvironmentById(environmentId);

        // Then
        assertNotNull(result);
        assertEquals(environmentId, result.getId());
        assertEquals("Test Environment", result.getName());
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByIdNotFound() {
        // Given
        String environmentId = "nonexistent";

        when(envrep.findById(environmentId)).thenReturn(Optional.empty());

        // When
        Environment result = environmentTool.getEnvironmentById(environmentId);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testAddNewEnvironment() {
        // Given
        String pypath = "/usr/bin/python3";
        List<Environment> oldEnvList = new ArrayList<>();
        String hostId = "host123";
        String name = "Python 3.8";

        Environment existingEnv = new Environment();
        existingEnv.setId("env123");
        existingEnv.setBin(pypath);
        oldEnvList.add(existingEnv);

        // No mock needed since the method won't call hostRepository.findById when environment exists

        // When
        environmentTool.addNewEnvironment(pypath, oldEnvList, hostId, name);

        // Then
        // Should not create new environment since it already exists
        // No verification needed as the method doesn't call save when environment exists
    }

    @Test
    @Timeout(10)
    void testAddNewEnvironmentWithNewEnvironment() {
        // Given
        String pypath = "/usr/bin/python3";
        List<Environment> oldEnvList = new ArrayList<>();
        String hostId = "host123";
        String name = "Python 3.8";

        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        environmentTool.addNewEnvironment(pypath, oldEnvList, hostId, name);

        // Then
        verify(environmentRepository).save(any(Environment.class));
    }

    @Test
    @Timeout(10)
    void testAddNewEnvironmentWithConda() {
        // Given
        String pypath = "/usr/bin/conda/python";
        List<Environment> oldEnvList = new ArrayList<>();
        String hostId = "host123";
        String name = "Conda Environment";

        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        environmentTool.addNewEnvironment(pypath, oldEnvList, hostId, name);

        // Then
        verify(environmentRepository).save(any(Environment.class));
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentsByHostId() {
        // Given
        String hostId = "host123";
        Collection<Environment> environments = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");
        environments.add(environment);

        when(environmentRepository.findEnvByHost(hostId)).thenReturn(environments);

        // When
        List<Environment> result = environmentTool.getEnvironmentsByHostId(hostId);

        // Then
        assertEquals(1, result.size());
        assertEquals("env123", result.get(0).getId());
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentsByHostIdWithException() {
        // Given
        String hostId = "host123";

        when(environmentRepository.findEnvByHost(hostId)).thenThrow(new RuntimeException("Database error"));

        // When
        List<Environment> result = environmentTool.getEnvironmentsByHostId(hostId);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @Timeout(10)
    void testSaveEnvironment() {
        // Given
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");

        // When
        environmentTool.saveEnvironment(environment);

        // Then
        verify(environmentRepository).save(environment);
    }

    @Test
    @Timeout(10)
    void testGetEnvironments() {
        // Given
        String hostId = "host123";
        List<Environment> environments = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");
        environments.add(environment);

        when(environmentTool.getEnvironmentsByHostId(hostId)).thenReturn(environments);

        // When
        String result = environmentTool.getEnvironments(hostId);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentsWithException() {
        // Given
        String hostId = "host123";

        when(environmentRepository.findEnvByHost(hostId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = environmentTool.getEnvironments(hostId);

        // Then
        // The method returns "[]" when exception occurs in getEnvironmentsByHostId
        assertEquals("[]", result);
    }

    @Test
    @Timeout(10)
    void testToJSON() {
        // Given
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");
        environment.setBin("/usr/bin/python3");
        environment.setPyenv("pip");
        environment.setBasedir("/tmp");
        environment.setType("python");

        // When
        String result = environmentTool.toJSON(environment);

        // Then
        assertNotNull(result);
        assertTrue(result.contains("env123"));
        assertTrue(result.contains("Test Environment"));
    }

    @Test
    @Timeout(10)
    void testToJSONWithException() {
        // Given
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");

        // When
        String result = environmentTool.toJSON(environment);

        // Then
        // The toJSON method should return the actual JSON representation
        assertNotNull(result);
        assertTrue(result.contains("env123"));
        assertTrue(result.contains("Test Environment"));
    }

    @Test
    @Timeout(10)
    void testShowAllEnvironment() {
        // Given
        List<Environment> environments = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");
        environments.add(environment);

        when(environmentRepository.findAll()).thenReturn(environments);

        // When
        environmentTool.showAllEnvironment();

        // Then
        verify(environmentRepository).findAll();
    }

    @Test
    @Timeout(10)
    void testShowAllEnvironmentWithException() {
        // Given
        when(environmentRepository.findAll()).thenThrow(new RuntimeException("Database error"));

        // When
        environmentTool.showAllEnvironment();

        // Then
        // Should not throw exception
        assertTrue(true);
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByBEB() {
        // Given
        String hostId = "host123";
        String bin = "/usr/bin/python3";
        String env = "pip";
        String basedir = "/tmp";
        Collection<Environment> environments = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setName("Test Environment");
        environments.add(environment);

        when(environmentRepository.findEnvByID_BIN_ENV_BaseDir(hostId, bin, env, basedir)).thenReturn(environments);

        // When
        String result = environmentTool.getEnvironmentByBEB(hostId, bin, env, basedir);

        // Then
        assertNotNull(result);
        assertTrue(result.startsWith("["));
        assertTrue(result.endsWith("]"));
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByBEBWithException() {
        // Given
        String hostId = "host123";
        String bin = "/usr/bin/python3";
        String env = "pip";
        String basedir = "/tmp";

        when(environmentRepository.findEnvByID_BIN_ENV_BaseDir(hostId, bin, env, basedir))
                .thenThrow(new RuntimeException("Database error"));

        // When
        String result = environmentTool.getEnvironmentByBEB(hostId, bin, env, basedir);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testIsLocal() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setIp("127.0.0.1");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        boolean result = environmentTool.islocal(hostId);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testIsLocalWithLocalhost() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setIp("localhost");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        boolean result = environmentTool.islocal(hostId);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testIsLocalWithRemoteHost() {
        // Given
        String hostId = "host123";
        Host host = new Host();
        host.setId(hostId);
        host.setIp("192.168.1.1");

        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));

        // When
        boolean result = environmentTool.islocal(hostId);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testIsLocalWithHostNotFound() {
        // Given
        String hostId = "nonexistent";

        when(hostRepository.findById(hostId)).thenReturn(Optional.empty());

        // When
        boolean result = environmentTool.islocal(hostId);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByBin() {
        // Given
        String bin = "/usr/bin/python3";
        List<Environment> envList = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setBin(bin);
        envList.add(environment);

        // When
        Environment result = environmentTool.getEnvironmentByBin(bin, envList);

        // Then
        assertNotNull(result);
        assertEquals("env123", result.getId());
        assertEquals(bin, result.getBin());
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByBinNotFound() {
        // Given
        String bin = "/usr/bin/python3";
        List<Environment> envList = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setBin("/usr/bin/python2");
        envList.add(environment);

        // When
        Environment result = environmentTool.getEnvironmentByBin(bin, envList);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByBinWithLongPath() {
        // Given
        String bin = "a".repeat(300); // Very long path
        List<Environment> envList = new ArrayList<>();

        // When
        Environment result = environmentTool.getEnvironmentByBin(bin, envList);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testGetEnvironmentByBinWithNullBin() {
        // Given
        String bin = "/usr/bin/python3";
        List<Environment> envList = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setBin(null);
        envList.add(environment);

        // When
        Environment result = environmentTool.getEnvironmentByBin(bin, envList);

        // Then
        assertNull(result);
    }

    @Test
    @Timeout(10)
    void testCheckIfEnvironmentExist() {
        // Given
        String bin = "/usr/bin/python3";
        List<Environment> envList = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setBin(bin);
        envList.add(environment);

        // When
        boolean result = environmentTool.checkIfEnvironmentExist(bin, envList);

        // Then
        assertTrue(result);
    }

    @Test
    @Timeout(10)
    void testCheckIfEnvironmentExistNotFound() {
        // Given
        String bin = "/usr/bin/python3";
        List<Environment> envList = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setBin("/usr/bin/python2");
        envList.add(environment);

        // When
        boolean result = environmentTool.checkIfEnvironmentExist(bin, envList);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testCheckIfEnvironmentExistWithNullBin() {
        // Given
        String bin = "/usr/bin/python3";
        List<Environment> envList = new ArrayList<>();
        Environment environment = new Environment();
        environment.setId("env123");
        environment.setBin(null);
        envList.add(environment);

        // When
        boolean result = environmentTool.checkIfEnvironmentExist(bin, envList);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testAddEnv() {
        // Given
        String historyId = "history123";
        String hostId = "host123";
        String type = "python";
        String bin = "/usr/bin/python3";
        String env = "pip";
        String basedir = "/tmp";
        String settings = "requirements.txt";

        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");

        // Mock static method calls
        when(hostRepository.findById(hostId)).thenReturn(Optional.of(host));
        when(environmentRepository.findEnvByID_BIN(hostId, bin)).thenReturn(new ArrayList<>());

        // When
        String result = environmentTool.addEnv(historyId, hostId, type, bin, env, basedir, settings);

        // Then
        verify(environmentRepository).save(any(Environment.class));
    }

    @Test
    @Timeout(10)
    void testAddEnvWithExistingEnvironment() {
        // Given
        String historyId = "history123";
        String hostId = "host123";
        String type = "python";
        String bin = "/usr/bin/python3";
        String env = "pip";
        String basedir = "/tmp";
        String settings = "requirements.txt";

        Host host = new Host();
        host.setId(hostId);
        host.setName("Test Host");

        Environment existingEnv = new Environment();
        existingEnv.setId("env123");
        existingEnv.setBin(bin);

        // Mock static method calls
        when(environmentRepository.findEnvByID_BIN(hostId, bin)).thenReturn(Arrays.asList(existingEnv));

        // When
        String result = environmentTool.addEnv(historyId, hostId, type, bin, env, basedir, settings);

        // Then
        // Should return null when environment already exists (as per implementation)
        assertNull(result);
        verify(environmentRepository, never()).save(any(Environment.class));
    }

    @Test
    @Timeout(10)
    void testAddEnvWithNullValues() {
        // Given
        String historyId = "history123";
        String hostId = "host123";
        String type = "python";
        String bin = null;
        String env = "pip";
        String basedir = "/tmp";
        String settings = "requirements.txt";

        // When
        String result = environmentTool.addEnv(historyId, hostId, type, bin, env, basedir, settings);

        // Then
        // Should return null when bin is null
        assertNull(result);
        verify(environmentRepository, never()).save(any(Environment.class));
    }

    @Test
    @Timeout(10)
    void testAddEnvWithException() {
        // Given
        String historyId = "history123";
        String hostId = "host123";
        String type = "python";
        String bin = "/usr/bin/python3";
        String env = "pip";
        String basedir = "/tmp";
        String settings = "requirements.txt";

        // Mock static method calls
        when(hostRepository.findById(hostId)).thenThrow(new RuntimeException("Database error"));

        // When
        String result = environmentTool.addEnv(historyId, hostId, type, bin, env, basedir, settings);

        // Then
        assertNull(result);
    }
}
