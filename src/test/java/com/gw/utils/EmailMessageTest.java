package com.gw.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class EmailMessageTest {

    @Test
    @Timeout(10)
    void testEmailMessageGettersAndSetters() {
        // Given
        EmailMessage emailMessage = new EmailMessage();
        String toAddress = "test@example.com";
        String subject = "Test Subject";
        String body = "Test Body";

        // When
        emailMessage.setTo_address(toAddress);
        emailMessage.setSubject(subject);
        emailMessage.setBody(body);

        // Then
        assertEquals(toAddress, emailMessage.getTo_address());
        assertEquals(subject, emailMessage.getSubject());
        assertEquals(body, emailMessage.getBody());
    }

    @Test
    @Timeout(10)
    void testEmailMessageWithNullValues() {
        // Given
        EmailMessage emailMessage = new EmailMessage();

        // When
        emailMessage.setTo_address(null);
        emailMessage.setSubject(null);
        emailMessage.setBody(null);

        // Then
        assertNull(emailMessage.getTo_address());
        assertNull(emailMessage.getSubject());
        assertNull(emailMessage.getBody());
    }

    @Test
    @Timeout(10)
    void testEmailMessageWithEmptyStrings() {
        // Given
        EmailMessage emailMessage = new EmailMessage();

        // When
        emailMessage.setTo_address("");
        emailMessage.setSubject("");
        emailMessage.setBody("");

        // Then
        assertEquals("", emailMessage.getTo_address());
        assertEquals("", emailMessage.getSubject());
        assertEquals("", emailMessage.getBody());
    }

    @Test
    @Timeout(10)
    void testEmailMessageWithSpecialCharacters() {
        // Given
        EmailMessage emailMessage = new EmailMessage();
        String toAddress = "test+tag@example.com";
        String subject = "Test Subject with Special Characters: !@#$%^&*()";
        String body = "Test Body with\nNew Lines and\tTabs";

        // When
        emailMessage.setTo_address(toAddress);
        emailMessage.setSubject(subject);
        emailMessage.setBody(body);

        // Then
        assertEquals(toAddress, emailMessage.getTo_address());
        assertEquals(subject, emailMessage.getSubject());
        assertEquals(body, emailMessage.getBody());
    }

    @Test
    @Timeout(10)
    void testEmailMessageWithLongStrings() {
        // Given
        EmailMessage emailMessage = new EmailMessage();
        String longString = "a".repeat(1000);

        // When
        emailMessage.setTo_address(longString);
        emailMessage.setSubject(longString);
        emailMessage.setBody(longString);

        // Then
        assertEquals(longString, emailMessage.getTo_address());
        assertEquals(longString, emailMessage.getSubject());
        assertEquals(longString, emailMessage.getBody());
    }

    @Test
    @Timeout(10)
    void testEmailMessageMultipleSetOperations() {
        // Given
        EmailMessage emailMessage = new EmailMessage();

        // When
        emailMessage.setTo_address("first@example.com");
        emailMessage.setTo_address("second@example.com");
        emailMessage.setSubject("First Subject");
        emailMessage.setSubject("Second Subject");
        emailMessage.setBody("First Body");
        emailMessage.setBody("Second Body");

        // Then
        assertEquals("second@example.com", emailMessage.getTo_address());
        assertEquals("Second Subject", emailMessage.getSubject());
        assertEquals("Second Body", emailMessage.getBody());
    }
}
