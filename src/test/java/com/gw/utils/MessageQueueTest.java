package com.gw.utils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class MessageQueueTest {

    private MessageQueue messageQueue;

    @BeforeEach
    void setUp() {
        messageQueue = new MessageQueue();
    }

    @Test
    @Timeout(10)
    void testAddMessageWithValidInput() {
        // Given
        String token = "test-token";
        String message = "test message";

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertTrue(result);
        assertTrue(messageQueue.hasMessages(token));
        assertEquals(1, messageQueue.getMessageCount(token));
    }

    @Test
    @Timeout(10)
    void testAddMessageWithNullToken() {
        // Given
        String token = null;
        String message = "test message";

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testAddMessageWithNullMessage() {
        // Given
        String token = "test-token";
        String message = null;

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testAddMessageWithEmptyToken() {
        // Given
        String token = "";
        String message = "test message";

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertTrue(result);
        assertTrue(messageQueue.hasMessages(token));
    }

    @Test
    @Timeout(10)
    void testAddMessageWithEmptyMessage() {
        // Given
        String token = "test-token";
        String message = "";

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertTrue(result);
        assertTrue(messageQueue.hasMessages(token));
    }

    @Test
    @Timeout(10)
    void testAddMultipleMessages() {
        // Given
        String token = "test-token";
        String message1 = "message 1";
        String message2 = "message 2";
        String message3 = "message 3";

        // When
        boolean result1 = messageQueue.addMessage(token, message1);
        boolean result2 = messageQueue.addMessage(token, message2);
        boolean result3 = messageQueue.addMessage(token, message3);

        // Then
        assertTrue(result1);
        assertTrue(result2);
        assertTrue(result3);
        assertEquals(3, messageQueue.getMessageCount(token));
    }

    @Test
    @Timeout(10)
    void testGetAndClearMessages() {
        // Given
        String token = "test-token";
        String message1 = "message 1";
        String message2 = "message 2";

        messageQueue.addMessage(token, message1);
        messageQueue.addMessage(token, message2);

        // When
        String[] messages = messageQueue.getAndClearMessages(token);

        // Then
        assertNotNull(messages);
        assertEquals(2, messages.length);
        assertEquals(message1, messages[0]);
        assertEquals(message2, messages[1]);
        assertFalse(messageQueue.hasMessages(token));
        assertEquals(0, messageQueue.getMessageCount(token));
    }

    @Test
    @Timeout(10)
    void testGetAndClearMessagesWithNullToken() {
        // Given
        String token = null;

        // When
        String[] messages = messageQueue.getAndClearMessages(token);

        // Then
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @Timeout(10)
    void testGetAndClearMessagesWithEmptyQueue() {
        // Given
        String token = "test-token";

        // When
        String[] messages = messageQueue.getAndClearMessages(token);

        // Then
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @Timeout(10)
    void testPeekMessages() {
        // Given
        String token = "test-token";
        String message1 = "message 1";
        String message2 = "message 2";

        messageQueue.addMessage(token, message1);
        messageQueue.addMessage(token, message2);

        // When
        String[] messages = messageQueue.peekMessages(token);

        // Then
        assertNotNull(messages);
        assertEquals(2, messages.length);
        assertEquals(message1, messages[0]);
        assertEquals(message2, messages[1]);
        assertTrue(messageQueue.hasMessages(token));
        assertEquals(2, messageQueue.getMessageCount(token));
    }

    @Test
    @Timeout(10)
    void testPeekMessagesWithNullToken() {
        // Given
        String token = null;

        // When
        String[] messages = messageQueue.peekMessages(token);

        // Then
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @Timeout(10)
    void testPeekMessagesWithEmptyQueue() {
        // Given
        String token = "test-token";

        // When
        String[] messages = messageQueue.peekMessages(token);

        // Then
        assertNotNull(messages);
        assertEquals(0, messages.length);
    }

    @Test
    @Timeout(10)
    void testHasMessages() {
        // Given
        String token = "test-token";
        String message = "test message";

        // When & Then
        assertFalse(messageQueue.hasMessages(token));
        
        messageQueue.addMessage(token, message);
        assertTrue(messageQueue.hasMessages(token));
    }

    @Test
    @Timeout(10)
    void testHasMessagesWithNullToken() {
        // Given
        String token = null;

        // When
        boolean result = messageQueue.hasMessages(token);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testClearMessages() {
        // Given
        String token = "test-token";
        String message = "test message";

        messageQueue.addMessage(token, message);
        assertTrue(messageQueue.hasMessages(token));

        // When
        messageQueue.clearMessages(token);

        // Then
        assertFalse(messageQueue.hasMessages(token));
        assertEquals(0, messageQueue.getMessageCount(token));
    }

    @Test
    @Timeout(10)
    void testClearMessagesWithNullToken() {
        // Given
        String token = null;

        // When
        messageQueue.clearMessages(token);

        // Then
        // Should not throw exception
        assertFalse(messageQueue.hasMessages(token));
    }

    @Test
    @Timeout(10)
    void testGetMessageCount() {
        // Given
        String token = "test-token";
        String message1 = "message 1";
        String message2 = "message 2";

        // When & Then
        assertEquals(0, messageQueue.getMessageCount(token));
        
        messageQueue.addMessage(token, message1);
        assertEquals(1, messageQueue.getMessageCount(token));
        
        messageQueue.addMessage(token, message2);
        assertEquals(2, messageQueue.getMessageCount(token));
    }

    @Test
    @Timeout(10)
    void testGetMessageCountWithNullToken() {
        // Given
        String token = null;

        // When
        int count = messageQueue.getMessageCount(token);

        // Then
        assertEquals(0, count);
    }

    @Test
    @Timeout(10)
    void testMultipleTokens() {
        // Given
        String token1 = "token1";
        String token2 = "token2";
        String message1 = "message for token1";
        String message2 = "message for token2";

        // When
        messageQueue.addMessage(token1, message1);
        messageQueue.addMessage(token2, message2);

        // Then
        assertTrue(messageQueue.hasMessages(token1));
        assertTrue(messageQueue.hasMessages(token2));
        assertEquals(1, messageQueue.getMessageCount(token1));
        assertEquals(1, messageQueue.getMessageCount(token2));
        
        String[] messages1 = messageQueue.getAndClearMessages(token1);
        String[] messages2 = messageQueue.getAndClearMessages(token2);
        
        assertEquals(1, messages1.length);
        assertEquals(1, messages2.length);
        assertEquals(message1, messages1[0]);
        assertEquals(message2, messages2[0]);
    }

    @Test
    @Timeout(10)
    void testSpecialCharactersInMessage() {
        // Given
        String token = "test-token";
        String message = "Special chars: !@#$%^&*()_+-=[]{}|;':\",./<>?";

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertTrue(result);
        assertTrue(messageQueue.hasMessages(token));
        
        String[] messages = messageQueue.getAndClearMessages(token);
        assertEquals(1, messages.length);
        assertEquals(message, messages[0]);
    }

    @Test
    @Timeout(10)
    void testLongMessage() {
        // Given
        String token = "test-token";
        String message = "a".repeat(1000);

        // When
        boolean result = messageQueue.addMessage(token, message);

        // Then
        assertTrue(result);
        assertTrue(messageQueue.hasMessages(token));
        
        String[] messages = messageQueue.getAndClearMessages(token);
        assertEquals(1, messages.length);
        assertEquals(message, messages[0]);
    }
}
