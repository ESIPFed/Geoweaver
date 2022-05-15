package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.SSHAuthenticationException;
import com.gw.ssh.SSHSession;
import com.gw.ssh.SSHSessionImpl;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class SSHSessionTest extends AbstractHelperMethodsTest{


    @Autowired
    UserTool ut;

    @Autowired
    BaseTool bt;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @Autowired
    SSHSession session;

    @LocalServerPort
    private int port;

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    void contextLoads() {

    }

    @Test
    @DisplayName("Test SSH login")
    void testLogin() {

        assertThrows(SSHAuthenticationException.class, () -> {
            session.login("100001", null, null, null, null, false);
        });

    }

    @Test
    void testLogin2() throws Exception {

        String hid = AddHost();

        assertThrows(SSHAuthenticationException.class, () -> {
            session.login(hid, null, null, false);
        });

    }

    @Test
    @DisplayName("Test running shell on ssh")
    void testShellOnSSH() {

        session.runBash("", "", "", false, "");

    }

    @Test
    @DisplayName("Test running shell on ssh")
    void testMultiShellOnSSH() {

        session.runMultipleBashes("", null, "");

    }

    @Test
    @DisplayName("Test running python on ssh")
    void testPythonOnSSH() {

        session.runPython("", "", "", false, "", null, null, null);

    }

    @Test
    @DisplayName("Test running jupyter on ssh")
    void testJupyterOnSSH() {

        session.runJupyter("", "", "", false, "", null, null, null);

    }

    @Test
    @DisplayName("Test running jupyter on ssh")
    void testFinalize() {

        session.logout();

    }

    @Test
    void testReadPython() {

        session.readPythonEnvironment(null, null);

    }

    @Test
    void testReadConda() throws IOException {

        assertThrows(NullPointerException.class, () -> {
            ((SSHSessionImpl) session).readWhereCondaInOneCommand(null);
        });

    }

    @Test
    void testSettersGettersSshSession() {

        SSHSessionImpl instance = new SSHSessionImpl();

        // instance.setHistory_process(null);
        // instance.setSSHJSession(null);
        // instance.setWebSocketSession(null);
        // instance.saveHistory(null, null);

        instance.getToken();
        instance.getHost();
        instance.getPort();
        instance.getSSHInput();
        instance.getSSHJSession();
        instance.getSSHOutput();
        instance.getSsh();
        instance.getToken();
        instance.getUsername();
        instance.isTerminal();

        instance.escapeJupter(null);

    }

}
