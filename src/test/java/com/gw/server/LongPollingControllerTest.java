package com.gw.server;

import com.gw.utils.MessageQueue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.async.DeferredResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LongPollingControllerTest {

    @Mock
    private MessageQueue messageQueue;

    private LongPollingController longPollingController;

    @BeforeEach
    void setUp() {
        longPollingController = new LongPollingController();
        // Inject the mock MessageQueue using reflection
        org.springframework.test.util.ReflectionTestUtils.setField(longPollingController, "messageQueue", messageQueue);
    }

    @Test
    void testPollMessagesWithExistingMessages() {
        // Given
        String token = "test-token";
        String[] messages = {"message1", "message2"};
        when(messageQueue.hasMessages(token)).thenReturn(true);
        when(messageQueue.getAndClearMessages(token)).thenReturn(messages);

        // When
        DeferredResult<ResponseEntity<?>> result = longPollingController.pollMessages(token);

        // Then
        assertNotNull(result);
        assertTrue(result.hasResult());
        ResponseEntity<?> response = (ResponseEntity<?>) result.getResult();
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(messages, (String[]) response.getBody());
    }

    @Test
    void testPollMessagesWithNoMessages() {
        // Given
        String token = "test-token";
        when(messageQueue.hasMessages(token)).thenReturn(false);

        // When
        DeferredResult<ResponseEntity<?>> result = longPollingController.pollMessages(token);

        // Then
        assertNotNull(result);
        assertFalse(result.hasResult()); // Should be pending
    }

    @Test
    void testSendMessageWithoutPendingRequest() {
        // Given
        String token = "test-token";
        String message = "test-message";
        when(messageQueue.addMessage(token, message)).thenReturn(true);

        // When
        ResponseEntity<?> response = longPollingController.sendMessage(token, message);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(messageQueue).addMessage(token, message);
    }

    @Test
    void testSendMessageWithQueueFailure() {
        // Given
        String token = "test-token";
        String message = "test-message";
        when(messageQueue.addMessage(token, message)).thenReturn(false);

        // When
        ResponseEntity<?> response = longPollingController.sendMessage(token, message);

        // Then
        assertEquals(HttpStatus.INSUFFICIENT_STORAGE, response.getStatusCode());
        assertEquals("Failed to queue message: queue full or other error", response.getBody());
    }

    @Test
    void testSendMessageToClientWithoutPendingRequest() {
        // Given
        String token = "test-token";
        String message = "test-message";
        when(messageQueue.addMessage(token, message)).thenReturn(true);

        // When
        boolean result = longPollingController.sendMessageToClient(token, message);

        // Then
        assertTrue(result);
        verify(messageQueue).addMessage(token, message);
    }

    @Test
    void testSendMessageToClientWithNullToken() {
        // When
        boolean result = longPollingController.sendMessageToClient(null, "test-message");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageToClientWithNullMessage() {
        // When
        boolean result = longPollingController.sendMessageToClient("test-token", null);

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageToClientWithEmptyToken() {
        // When
        boolean result = longPollingController.sendMessageToClient("", "test-message");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageToClientWithEmptyMessage() {
        // When
        boolean result = longPollingController.sendMessageToClient("test-token", "");

        // Then
        assertFalse(result);
    }

    @Test
    void testSendMessageToClientWithQueueFailure() {
        // Given
        String token = "test-token";
        String message = "test-message";
        when(messageQueue.addMessage(token, message)).thenReturn(false);

        // When
        boolean result = longPollingController.sendMessageToClient(token, message);

        // Then
        assertFalse(result);
        verify(messageQueue).addMessage(token, message);
    }
}