package com.gw;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.ssh.SSHSession;
import com.gw.server.FileUploadServlet;
import com.gw.server.TerminalServlet;
import com.gw.server.TestSocketServlet;
import com.gw.server.WorkflowServlet;
import com.gw.tools.UserTool;
import com.gw.utils.BaseTool;

import org.apache.commons.fileupload.FileItem;
import org.apache.log4j.Logger;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = GeoweaverApplication.class)
public class ServletTest extends AbstractHelperMethodsTest {

    @Autowired
    UserTool ut;

    @Autowired
    BaseTool bt;

    @Autowired
    private TestRestTemplate testrestTemplate;

    @Autowired
    TerminalServlet servlet;

    @Autowired
    WorkflowServlet wfServlet;

    FileUploadServlet fileServlet;

    @LocalServerPort
    private int port;

    Logger logger = Logger.getLogger(this.getClass());

    @Test
    @DisplayName("Test Terminal OnOpen Websocket Message")
    void testTerminalOnOpen() {

        Session session = Mockito.mock(Session.class);

        servlet.open(session, null);

    }

    @Test
    @DisplayName("Test Terminal OnClose Websocket Message")
    void testTerminalOnClose() {

        Session session = Mockito.mock(Session.class);
        servlet.close(session);

    }

    @Test
    @DisplayName("Test Terminal Error Websocket Message")
    void testTerminalError() {

        try {

            servlet.error(null, new RuntimeException("Test Terminal Websocket Error"));

        } catch (Throwable e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Terminal Echo Websocket Message")
    void testTerminalEcho() {

        Session session = Mockito.mock(Session.class);
        servlet.echo("Test Terminal Echo", session);

    }

    @Test
    @DisplayName("Test Terminal Find Session")
    void testTerminalFindBySession() {

        servlet.findSessionById("");

    }

    @Test
    @DisplayName("Test Workflow Socket Registration")
    void testWorkflowRegisterSession() {

        // Session session = new Session();
        try {
            wfServlet.registerSession(null, "");

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Workflow OnOpen Message")
    void testWorkflowOnOpen() {

        try {

            wfServlet.open(null, null);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Workflow OnClose Message")
    void testWorkflowOnClose() {

        try {

            wfServlet.close(null);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Workflow Error Message")
    void testWorkflowError() {

        try {
            wfServlet.error(null, new Throwable("Test Workflow Socket Error"));

        } catch (Throwable e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Workflow Echo Message")
    void testWorkflowEcho() {

        try {

            wfServlet.echo("Test Terminal Echo", null);

        } catch (Exception e) {
            // TODO: handle exception
        }

    }

    @Test
    @DisplayName("Test Workflow findSessionByToken")
    void testWorkflowFindSessionByToken() {
        // Test fails with "NullPointerException"

        WorkflowServlet.findSessionByToken("");

    }

}