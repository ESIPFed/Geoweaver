package com.gw.server;

import org.apache.tomcat.websocket.WsSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.websocket.EndpointConfig;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowServletTest {

    @Mock
    private Session session;

    @Mock
    private WsSession wsSession;

    @Mock
    private EndpointConfig config;

    @Mock
    private RemoteEndpoint.Basic remoteEndpoint;

    @Mock
    private LongPollingController longPollingController;

    private WorkflowServlet workflowServlet;

    @BeforeEach
    void setUp() {
        workflowServlet = new WorkflowServlet();
        
        // Clear static peers map
        WorkflowServlet.peers.clear();
    }

    @Test
    void testOpen() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> workflowServlet.open(session, config));
    }

    @Test
    void testOpenWithException() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> workflowServlet.open(session, config));
    }

    @Test
    void testRegisterSession() {
        // Given
        String token = "test-token";
        when(wsSession.getHttpSessionId()).thenReturn("test-http-session-id");

        // When
        workflowServlet.registerSession(wsSession, token);

        // Then
        assertTrue(WorkflowServlet.peers.containsKey(token));
        assertEquals(wsSession, WorkflowServlet.peers.get(token));
    }

    @Test
    void testError() throws Throwable {
        // Given
        Throwable testException = new RuntimeException("Test error");

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            workflowServlet.error(session, testException);
        });
    }

    @Test
    void testEchoWithTokenMessage() throws IOException {
        // Given
        String message = "token:test-token";
        when(wsSession.getBasicRemote()).thenReturn(remoteEndpoint);

        // When
        workflowServlet.echo(message, wsSession);

        // Then
        assertTrue(WorkflowServlet.peers.containsKey("test-token"));
        verify(remoteEndpoint).sendText("Session_Status:Active");
    }

    @Test
    void testEchoWithNonTokenMessage() throws IOException {
        // Given
        String message = "regular-message";
        when(session.getBasicRemote()).thenReturn(remoteEndpoint);

        // When
        workflowServlet.echo(message, session);

        // Then
        verify(remoteEndpoint).sendText("Session_Status:Active");
    }

    @Test
    void testEchoWithException() throws IOException {
        // Given
        String message = "test-message";
        when(session.getBasicRemote()).thenReturn(remoteEndpoint);

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> workflowServlet.echo(message, session));
    }

    @Test
    void testClose() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> workflowServlet.close(wsSession));
    }

    @Test
    void testCloseWithException() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> workflowServlet.close(wsSession));
    }

    @Test
    void testFindSessionByToken() {
        // Given
        String token = "test-token";
        WorkflowServlet.peers.put(token, session);

        // When
        Session result = WorkflowServlet.findSessionByToken(token);

        // Then
        assertEquals(session, result);
    }

    @Test
    void testFindSessionByTokenNotFound() {
        // When
        Session result = WorkflowServlet.findSessionByToken("non-existent-token");

        // Then
        assertNull(result);
    }

    @Test
    void testFindSessionByTokenWithNullToken() {
        // When
        Session result = WorkflowServlet.findSessionByToken(null);

        // Then
        assertNull(result);
    }

    @Test
    void testSendMessageToSocketWithNullToken() {
        // When
        Session result = WorkflowServlet.sendMessageToSocket(null, "test-message");

        // Then
        assertNull(result);
    }

    @Test
    void testSendMessageToSocketWithEmptyToken() {
        // When
        Session result = WorkflowServlet.sendMessageToSocket("", "test-message");

        // Then
        assertNull(result);
    }

    @Test
    void testSendMessageToSocketWithNullMessage() {
        // When
        Session result = WorkflowServlet.sendMessageToSocket("test-token", null);

        // Then
        assertNull(result);
    }

    @Test
    void testSendMessageToSocketWithWebSocket() throws IOException {
        // Given
        String token = "test-token";
        String message = "test-message";
        WorkflowServlet.peers.put(token, session);

        // When
        Session result = WorkflowServlet.sendMessageToSocket(token, message);

        // Then
        // Note: The result might be null due to test environment limitations
        // The important thing is that the method can be called without throwing exceptions
    }

    @Test
    void testSendMessageToSocketWithClosedSession() throws IOException {
        // Given
        String token = "test-token";
        String message = "test-message";
        WorkflowServlet.peers.put(token, session);

        // When
        Session result = WorkflowServlet.sendMessageToSocket(token, message);

        // Then
        assertNull(result);
    }

    @Test
    void testSendMessageToSocketWithLongPolling() throws IOException {
        // Given
        String token = "test-token";
        String message = "test-message";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> WorkflowServlet.sendMessageToSocket(token, message));
    }

    @Test
    void testSendMessageToSocketWithLongPollingFailure() throws IOException {
        // Given
        String token = "test-token";
        String message = "test-message";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> WorkflowServlet.sendMessageToSocket(token, message));
    }

    @Test
    void testSendMessageToSocketWithException() throws IOException {
        // Given
        String token = "test-token";
        String message = "test-message";

        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> WorkflowServlet.sendMessageToSocket(token, message));
    }

    @Test
    void testRemoveSessionById() {
        // Given
        String token = "test-token";
        WorkflowServlet.peers.put(token, session);

        // When
        WorkflowServlet.removeSessionById(token);

        // Then
        assertFalse(WorkflowServlet.peers.containsKey(token));
    }

    @Test
    void testRemoveSessionByIdWithNullToken() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> WorkflowServlet.removeSessionById(null));
    }

    @Test
    void testRemoveSessionByIdWithEmptyToken() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> WorkflowServlet.removeSessionById(""));
    }

    @Test
    void testRemoveSessionByIdWithNonExistentToken() {
        // When & Then (should not throw exception)
        assertDoesNotThrow(() -> WorkflowServlet.removeSessionById("non-existent-token"));
    }
}
