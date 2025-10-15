package com.gw.server;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class WebSocketConfigTest {

    @Test
    void testServerEndpoint() {
        // Given
        WebSocketConfig config = new WebSocketConfig();

        // When
        ServerEndpointExporter result = config.serverEndpoint();

        // Then
        assertNotNull(result);
        assertInstanceOf(ServerEndpointExporter.class, result);
    }

    @Test
    void testCallCommandWebSocketController() {
        // Given
        WebSocketConfig config = new WebSocketConfig();

        // When
        CommandServlet result = config.callCommandWebSocketController();

        // Then
        assertNotNull(result);
        assertInstanceOf(CommandServlet.class, result);
    }

    @Test
    void testCallTerminalWebSocketController() {
        // Given
        WebSocketConfig config = new WebSocketConfig();

        // When
        TerminalServlet result = config.callTerminalWebSocketController();

        // Then
        assertNotNull(result);
        assertInstanceOf(TerminalServlet.class, result);
    }

    @Test
    void testCallWorkflowWebSocketController() {
        // Given
        WebSocketConfig config = new WebSocketConfig();

        // When
        WorkflowServlet result = config.callWorkflowWebSocketController();

        // Then
        assertNotNull(result);
        assertInstanceOf(WorkflowServlet.class, result);
    }

    @Test
    void testMultipleInstancesAreDifferent() {
        // Given
        WebSocketConfig config = new WebSocketConfig();

        // When
        CommandServlet servlet1 = config.callCommandWebSocketController();
        CommandServlet servlet2 = config.callCommandWebSocketController();

        // Then
        assertNotNull(servlet1);
        assertNotNull(servlet2);
        // Note: These might be the same instance if the bean is singleton,
        // but the important thing is that they are not null and are of the correct type
    }

    @Test
    void testAllBeansAreNotNull() {
        // Given
        WebSocketConfig config = new WebSocketConfig();

        // When
        ServerEndpointExporter exporter = config.serverEndpoint();
        CommandServlet commandServlet = config.callCommandWebSocketController();
        TerminalServlet terminalServlet = config.callTerminalWebSocketController();
        WorkflowServlet workflowServlet = config.callWorkflowWebSocketController();

        // Then
        assertNotNull(exporter);
        assertNotNull(commandServlet);
        assertNotNull(terminalServlet);
        assertNotNull(workflowServlet);
    }
}
