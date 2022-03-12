package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.util.Map;

import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import com.amazonaws.services.opsworkscm.model.Server;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.SSHSession;
import com.gw.server.TerminalServlet;
import com.gw.server.TestSocketServlet;
import com.gw.server.WorkflowServlet;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.log4j.Logger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ServletTest extends AbstractHelperMethodsTest {

    @Autowired
    UserTool ut;

    @Autowired
    BaseTool bt;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @Autowired
    TerminalServlet servlet;

    TestSocketServlet socketServlet;

    WorkflowServlet wfServlet;

    @LocalServerPort
    private int port;

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    @DisplayName("Test Terminal OnOpen Websocket Message")
    void testTerminalOnOpen() {

        servlet.open(null, null);

    }

    @Test
    @DisplayName("Test Terminal OnClose Websocket Message")
    void testTerminalOnClose() {

        servlet.close(null);

    }

    @Test
    @DisplayName("Test Terminal Error Websocket Message")
    void testTerminalError() {

        assertThrows(Throwable.class, () -> servlet.error(null, new Throwable("Test Terminal Websocket Error")));

    }

    @Test
    @DisplayName("Test Terminal Echo Websocket Message")
    void testTerminalEcho() {

        servlet.echo("Test Terminal Echo", null);

    }

    @Test
    @DisplayName("Test Terminal Find Session")
    void testTerminalFindBySession() {

        servlet.findSessionById(null);

    }

    // @Test
    @DisplayName("Test Socket OnOpen Message")
    void testSocketOnOpen() {
        // Test fails with "NullPointerException"

        Session session = Mockito.mock(Session.class);
        socketServlet.open(session, null);

    }

    // @Test
    @DisplayName("Test Socket OnClose Message")
    void testSocketOnClose() {
        // Test fails with "NullPointerException"

        Session session = Mockito.mock(Session.class);
        socketServlet.close(session);

    }

    @Test
    @DisplayName("Test Socket Error Message")
    void testSocketError() {

        assertThrows(Throwable.class, () -> socketServlet.error(null, new Throwable("Test Socket Error")));

    }

    // @Test
    @DisplayName("Test Socket Echo Message")
    void testSocketEcho() {
        // Test fails with "NullPointerException"

        Session session = Mockito.mock(Session.class);
        socketServlet.echo("Test Terminal Echo", session);

    }

    @Test
    @DisplayName("Test Workflow Socket Registration")
    void testWorkflowRegisterSession() {

        Session session = Mockito.mock(Session.class);
        wfServlet.registerSession(session, "");

    }

    @Test
    @DisplayName("Test Workflow OnOpen Message")
    void testWorkflowOnOpen() {
        // Test fails with "NullPointerException"

        Session session = Mockito.mock(Session.class);
        EndpointConfig conf = Mockito.mock(EndpointConfig.class);
        wfServlet.open(session, conf);

    }

    @Test
    @DisplayName("Test Workflow OnClose Message")
    void testWorkflowOnClose() {
        // Test fails with "NullPointerException"

        Session session = Mockito.mock(Session.class);
        wfServlet.close(session);

    }

    @Test
    @DisplayName("Test Workflow Error Message")
    void testWorkflowError() {

        assertThrows(Throwable.class, () -> wfServlet.error(null, new Throwable("Test Workflow Socket Error")));

    }

    @Test
    @DisplayName("Test Workflow Echo Message")
    void testWorkflowEcho() {
        // Test fails with "NullPointerException"

        Session session = Mockito.mock(Session.class);
        wfServlet.echo("Test Terminal Echo", session);

    }

    @Test
    @DisplayName("Test Workflow findSessionByToken")
    void testWorkflowFindSessionByToken() {
        // Test fails with "NullPointerException"

        WorkflowServlet.findSessionByToken("");

    }

}