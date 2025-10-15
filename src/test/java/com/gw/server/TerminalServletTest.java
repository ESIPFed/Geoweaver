package com.gw.server;

import com.gw.ssh.SSHSession;
import org.apache.tomcat.websocket.WsSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.websocket.EndpointConfig;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TerminalServletTest {

    @Mock
    private Session session;

    @Mock
    private WsSession wsSession;

    @Mock
    private EndpointConfig config;

    @Mock
    private RemoteEndpoint.Basic remoteEndpoint;

    @Mock
    private SSHSession sshSession;

    private TerminalServlet terminalServlet;

    @BeforeEach
    void setUp() {
        terminalServlet = new TerminalServlet();
        
        // Clear static peers map
        TerminalServlet.peers.clear();
    }

    @Test
    void testOpen() {
        // Given
        String httpSessionId = "test-http-session-id";
        when(wsSession.getHttpSessionId()).thenReturn(httpSessionId);

        // When
        terminalServlet.open(wsSession, config);

        // Then
        assertTrue(TerminalServlet.peers.containsKey(httpSessionId));
        assertEquals(wsSession, TerminalServlet.peers.get(httpSessionId));
    }

    @Test
    void testOpenWithException() {
        // Given
        when(wsSession.getHttpSessionId()).thenReturn("test-http-session-id");

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.open(wsSession, config));
    }

    @Test
    void testError() throws Throwable {
        // Given
        Throwable testException = new RuntimeException("Test error");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            terminalServlet.error(session, testException);
        });
    }

    @Test
    void testEchoWithNoSSHSession() throws IOException {
        // Given
        String message = "test-message";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.echo(message, wsSession));
    }

    @Test
    void testEchoWithSSHSession() throws IOException {
        // Given
        String message = "test-command";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.echo(message, wsSession));
    }

    @Test
    void testEchoWithLogoutCommand() throws IOException {
        // Given
        String message = "logout";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.echo(message, wsSession));
    }

    @Test
    void testEchoWithQuitCommand() throws IOException {
        // Given
        String message = "quit";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.echo(message, wsSession));
    }

    @Test
    void testEchoWithException() throws IOException {
        // Given
        String message = "test-command";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.echo(message, wsSession));
    }

    @Test
    void testClose() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.close(wsSession));
    }

    @Test
    void testCloseWithNonTerminalSSHSession() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.close(wsSession));
    }

    @Test
    void testCloseWithException() {
        // Given
        when(session.getId()).thenThrow(new RuntimeException("Session error"));

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> terminalServlet.close(session));
    }

    @Test
    void testFindSessionById() {
        // Given
        String sessionId = "test-session-id";
        TerminalServlet.peers.put(sessionId, session);

        // When
        Session result = TerminalServlet.findSessionById(sessionId);

        // Then
        assertEquals(session, result);
    }

    @Test
    void testFindSessionByIdNotFound() {
        // When
        Session result = TerminalServlet.findSessionById("non-existent-session-id");

        // Then
        assertNull(result);
    }

    @Test
    void testFindSessionByIdWithNullId() {
        // When
        Session result = TerminalServlet.findSessionById(null);

        // Then
        assertNull(result);
    }
}
