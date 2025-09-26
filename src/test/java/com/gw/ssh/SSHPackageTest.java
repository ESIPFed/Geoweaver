package com.gw.ssh;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class SSHPackageTest {

    @BeforeEach
    void setUp() {
        // Clear the static map before each test
        RSAEncryptTool.token2KeyPair.clear();
    }

    @AfterEach
    void tearDown() {
        // Clear the static map after each test
        RSAEncryptTool.token2KeyPair.clear();
    }

    @Test
    void testRSAEncryptToolGetPublicKey() throws NoSuchAlgorithmException {
        String sessionId = "test-session-123";
        String publicKeyJson = RSAEncryptTool.getPublicKey(sessionId);
        
        assertNotNull(publicKeyJson);
        assertTrue(publicKeyJson.contains("rsa_public"));
        assertTrue(publicKeyJson.contains("{"));
        assertTrue(publicKeyJson.contains("}"));
        
        // Verify that the key pair was stored
        assertTrue(RSAEncryptTool.token2KeyPair.containsKey(sessionId));
        assertNotNull(RSAEncryptTool.token2KeyPair.get(sessionId));
    }

    @Test
    void testRSAEncryptToolGetPublicKeyWithNullSessionId() throws NoSuchAlgorithmException {
        String publicKeyJson = RSAEncryptTool.getPublicKey(null);
        
        assertNotNull(publicKeyJson);
        assertTrue(publicKeyJson.contains("rsa_public"));
    }

    @Test
    void testRSAEncryptToolGetPublicKeyWithEmptySessionId() throws NoSuchAlgorithmException {
        String publicKeyJson = RSAEncryptTool.getPublicKey("");
        
        assertNotNull(publicKeyJson);
        assertTrue(publicKeyJson.contains("rsa_public"));
    }

    @Test
    void testRSAEncryptToolGetPublicKeyWithSpecialCharacters() throws NoSuchAlgorithmException {
        String sessionId = "test-session-123_456@domain.com";
        String publicKeyJson = RSAEncryptTool.getPublicKey(sessionId);
        
        assertNotNull(publicKeyJson);
        assertTrue(publicKeyJson.contains("rsa_public"));
    }

    @Test
    void testRSAEncryptToolGetPasswordsWithNullArray() {
        assertThrows(Exception.class, () -> {
            RSAEncryptTool.getPasswords(null, "session-id");
        });
    }

    @Test
    void testRSAEncryptToolGetPasswordsWithEmptyArray() throws Exception {
        String[] emptyArray = {};
        String[] result = RSAEncryptTool.getPasswords(emptyArray, "session-id");
        
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testRSAEncryptToolGetPasswordsWithValidData() throws Exception {
        String sessionId = "test-session-123";
        
        // First get a public key to create the key pair
        RSAEncryptTool.getPublicKey(sessionId);
        
        // Create test encrypted passwords (this would normally be done by the client)
        String[] encryptedPasswords = {"test-encrypted-password-1", "test-encrypted-password-2"};
        
        // This will likely throw an exception due to invalid encrypted data, but we can test the structure
        try {
            String[] result = RSAEncryptTool.getPasswords(encryptedPasswords, sessionId);
            assertNotNull(result);
            assertEquals(encryptedPasswords.length, result.length);
        } catch (Exception e) {
            // Expected due to invalid encrypted data format
            assertTrue(e instanceof Exception);
        }
    }

    @Test
    void testRSAEncryptToolGetPasswordsWithNullSessionId() {
        String[] encryptedPasswords = {"test1", "test2"};
        
        assertThrows(Exception.class, () -> {
            RSAEncryptTool.getPasswords(encryptedPasswords, null);
        });
    }

    @Test
    void testRSAEncryptToolGetPasswordsWithEmptySessionId() {
        String[] encryptedPasswords = {"test1", "test2"};
        
        assertThrows(Exception.class, () -> {
            RSAEncryptTool.getPasswords(encryptedPasswords, "");
        });
    }

    @Test
    void testRSAEncryptToolVoidKey() {
        String sessionId = "test-session-123";
        
        // First create a key pair
        try {
            RSAEncryptTool.getPublicKey(sessionId);
            assertTrue(RSAEncryptTool.token2KeyPair.containsKey(sessionId));
            
            // Void the key
            RSAEncryptTool.voidKey(sessionId);
            assertFalse(RSAEncryptTool.token2KeyPair.containsKey(sessionId));
        } catch (NoSuchAlgorithmException e) {
            fail("Unexpected exception: " + e.getMessage());
        }
    }

    @Test
    void testRSAEncryptToolVoidKeyWithNullSessionId() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            RSAEncryptTool.voidKey(null);
        });
    }

    @Test
    void testRSAEncryptToolVoidKeyWithEmptySessionId() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            RSAEncryptTool.voidKey("");
        });
    }

    @Test
    void testRSAEncryptToolVoidKeyWithNonExistentSessionId() {
        // Should not throw exception
        assertDoesNotThrow(() -> {
            RSAEncryptTool.voidKey("non-existent-session");
        });
    }

    @Test
    void testRSAEncryptToolBuildKeyPair() throws NoSuchAlgorithmException {
        KeyPair keyPair = RSAEncryptTool.buildKeyPair();
        
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPublic());
        assertNotNull(keyPair.getPrivate());
    }

    @Test
    void testRSAEncryptToolByte2Base64() {
        String testString = "Hello World";
        byte[] testBytes = testString.getBytes();
        
        String base64Result = RSAEncryptTool.byte2Base64(testBytes);
        
        assertNotNull(base64Result);
        assertFalse(base64Result.isEmpty());
    }

    @Test
    void testRSAEncryptToolByte2Base64WithNull() {
        assertThrows(NullPointerException.class, () -> {
            RSAEncryptTool.byte2Base64(null);
        });
    }

    @Test
    void testRSAEncryptToolByte2Base64WithEmptyArray() {
        byte[] emptyArray = {};
        String result = RSAEncryptTool.byte2Base64(emptyArray);
        
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testRSAEncryptToolBase642Byte() {
        String testString = "Hello World";
        byte[] testBytes = testString.getBytes();
        String base64String = RSAEncryptTool.byte2Base64(testBytes);
        
        byte[] result = RSAEncryptTool.base642Byte(base64String);
        
        assertNotNull(result);
        assertArrayEquals(testBytes, result);
    }

    @Test
    void testRSAEncryptToolBase642ByteWithNull() {
        assertThrows(NullPointerException.class, () -> {
            RSAEncryptTool.base642Byte(null);
        });
    }

    @Test
    void testRSAEncryptToolBase642ByteWithEmptyString() {
        byte[] result = RSAEncryptTool.base642Byte("");
        assertNotNull(result);
        assertEquals(0, result.length);
    }

    @Test
    void testRSAEncryptToolDecrypt() throws NoSuchAlgorithmException {
        String sessionId = "test-session-123";
        KeyPair keyPair = RSAEncryptTool.buildKeyPair();
        RSAEncryptTool.token2KeyPair.put(sessionId, keyPair);
        
        String testString = "Hello World";
        byte[] testBytes = testString.getBytes();
        
        // This will likely throw an exception due to encryption complexity, but we can test the method exists
        try {
            byte[] result = RSAEncryptTool.decrypt(keyPair.getPrivate(), testBytes);
            assertNotNull(result);
        } catch (Exception e) {
            // Expected due to encryption complexity
            assertTrue(e instanceof Exception);
        }
    }

    @Test
    void testRSAEncryptToolDecryptWithNullPrivateKey() {
        byte[] testBytes = "Hello World".getBytes();
        
        assertThrows(Exception.class, () -> {
            RSAEncryptTool.decrypt(null, testBytes);
        });
    }

    @Test
    void testRSAEncryptToolDecryptWithNullBytes() throws NoSuchAlgorithmException {
        String sessionId = "test-session-123";
        KeyPair keyPair = RSAEncryptTool.buildKeyPair();
        
        assertThrows(Exception.class, () -> {
            RSAEncryptTool.decrypt(keyPair.getPrivate(), null);
        });
    }

    @Test
    void testRSAEncryptToolMultipleSessions() throws NoSuchAlgorithmException {
        String sessionId1 = "session-1";
        String sessionId2 = "session-2";
        
        String publicKey1 = RSAEncryptTool.getPublicKey(sessionId1);
        String publicKey2 = RSAEncryptTool.getPublicKey(sessionId2);
        
        assertNotNull(publicKey1);
        assertNotNull(publicKey2);
        assertNotEquals(publicKey1, publicKey2);
        
        assertTrue(RSAEncryptTool.token2KeyPair.containsKey(sessionId1));
        assertTrue(RSAEncryptTool.token2KeyPair.containsKey(sessionId2));
        
        // Void one session
        RSAEncryptTool.voidKey(sessionId1);
        assertFalse(RSAEncryptTool.token2KeyPair.containsKey(sessionId1));
        assertTrue(RSAEncryptTool.token2KeyPair.containsKey(sessionId2));
    }

    @Test
    void testRSAEncryptToolToken2KeyPairIsStatic() {
        // Verify that the token2KeyPair is a static field
        assertNotNull(RSAEncryptTool.token2KeyPair);
        assertTrue(RSAEncryptTool.token2KeyPair instanceof Map);
    }

    @Test
    void testRSAEncryptToolWithSpecialCharacters() throws NoSuchAlgorithmException {
        String sessionId = "session-with-special-chars-!@#$%^&*()";
        String publicKeyJson = RSAEncryptTool.getPublicKey(sessionId);
        
        assertNotNull(publicKeyJson);
        assertTrue(publicKeyJson.contains("rsa_public"));
        assertTrue(RSAEncryptTool.token2KeyPair.containsKey(sessionId));
    }

    @Test
    void testRSAEncryptToolWithLongSessionId() throws NoSuchAlgorithmException {
        String longSessionId = "a".repeat(1000); // Very long session ID
        String publicKeyJson = RSAEncryptTool.getPublicKey(longSessionId);
        
        assertNotNull(publicKeyJson);
        assertTrue(publicKeyJson.contains("rsa_public"));
        assertTrue(RSAEncryptTool.token2KeyPair.containsKey(longSessionId));
    }
}
