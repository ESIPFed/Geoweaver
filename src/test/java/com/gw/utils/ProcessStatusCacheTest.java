package com.gw.utils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class ProcessStatusCacheTest {

    private ProcessStatusCache processStatusCache;

    @BeforeEach
    void setUp() {
        processStatusCache = new ProcessStatusCache();
    }

    @AfterEach
    void tearDown() {
        if (processStatusCache != null) {
            processStatusCache.shutdown();
        }
    }

    @Test
    @Timeout(10)
    void testUpdateStatusWithValidInput() {
        // Given
        String historyId = "test-history-123";
        String status = "RUNNING";

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertTrue(processStatusCache.containsStatus(historyId));
        assertEquals(status, processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testUpdateStatusWithNullHistoryId() {
        // Given
        String historyId = null;
        String status = "RUNNING";

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertFalse(processStatusCache.containsStatus(historyId));
        assertNull(processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testUpdateStatusWithNullStatus() {
        // Given
        String historyId = "test-history-123";
        String status = null;

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertFalse(processStatusCache.containsStatus(historyId));
        assertNull(processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testUpdateStatusWithEmptyStrings() {
        // Given
        String historyId = "";
        String status = "";

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertTrue(processStatusCache.containsStatus(historyId));
        assertEquals(status, processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testUpdateStatusMultipleTimes() {
        // Given
        String historyId = "test-history-123";
        String status1 = "RUNNING";
        String status2 = "COMPLETED";
        String status3 = "FAILED";

        // When
        processStatusCache.updateStatus(historyId, status1);
        assertEquals(status1, processStatusCache.getStatus(historyId));
        
        processStatusCache.updateStatus(historyId, status2);
        assertEquals(status2, processStatusCache.getStatus(historyId));
        
        processStatusCache.updateStatus(historyId, status3);
        assertEquals(status3, processStatusCache.getStatus(historyId));

        // Then
        assertTrue(processStatusCache.containsStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testGetStatusWithNonExistentHistoryId() {
        // Given
        String historyId = "non-existent-history";

        // When
        String status = processStatusCache.getStatus(historyId);

        // Then
        assertNull(status);
        assertFalse(processStatusCache.containsStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testGetStatusWithNullHistoryId() {
        // Given
        String historyId = null;

        // When
        String status = processStatusCache.getStatus(historyId);

        // Then
        assertNull(status);
        assertFalse(processStatusCache.containsStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testRemoveStatus() {
        // Given
        String historyId = "test-history-123";
        String status = "RUNNING";

        processStatusCache.updateStatus(historyId, status);
        assertTrue(processStatusCache.containsStatus(historyId));

        // When
        processStatusCache.removeStatus(historyId);

        // Then
        assertFalse(processStatusCache.containsStatus(historyId));
        assertNull(processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testRemoveStatusWithNullHistoryId() {
        // Given
        String historyId = null;

        // When
        processStatusCache.removeStatus(historyId);

        // Then
        // Should not throw exception
        assertFalse(processStatusCache.containsStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testRemoveStatusWithNonExistentHistoryId() {
        // Given
        String historyId = "non-existent-history";

        // When
        processStatusCache.removeStatus(historyId);

        // Then
        // Should not throw exception
        assertFalse(processStatusCache.containsStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testContainsStatus() {
        // Given
        String historyId = "test-history-123";
        String status = "RUNNING";

        // When & Then
        assertFalse(processStatusCache.containsStatus(historyId));
        
        processStatusCache.updateStatus(historyId, status);
        assertTrue(processStatusCache.containsStatus(historyId));
        
        processStatusCache.removeStatus(historyId);
        assertFalse(processStatusCache.containsStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testContainsStatusWithNullHistoryId() {
        // Given
        String historyId = null;

        // When
        boolean result = processStatusCache.containsStatus(historyId);

        // Then
        assertFalse(result);
    }

    @Test
    @Timeout(10)
    void testMultipleHistoryIds() {
        // Given
        String historyId1 = "history-1";
        String historyId2 = "history-2";
        String historyId3 = "history-3";
        String status1 = "RUNNING";
        String status2 = "COMPLETED";
        String status3 = "FAILED";

        // When
        processStatusCache.updateStatus(historyId1, status1);
        processStatusCache.updateStatus(historyId2, status2);
        processStatusCache.updateStatus(historyId3, status3);

        // Then
        assertTrue(processStatusCache.containsStatus(historyId1));
        assertTrue(processStatusCache.containsStatus(historyId2));
        assertTrue(processStatusCache.containsStatus(historyId3));
        
        assertEquals(status1, processStatusCache.getStatus(historyId1));
        assertEquals(status2, processStatusCache.getStatus(historyId2));
        assertEquals(status3, processStatusCache.getStatus(historyId3));
    }

    @Test
    @Timeout(10)
    void testSpecialCharactersInHistoryId() {
        // Given
        String historyId = "history-with-special-chars-!@#$%^&*()";
        String status = "RUNNING";

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertTrue(processStatusCache.containsStatus(historyId));
        assertEquals(status, processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testLongHistoryId() {
        // Given
        String historyId = "a".repeat(1000);
        String status = "RUNNING";

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertTrue(processStatusCache.containsStatus(historyId));
        assertEquals(status, processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testLongStatus() {
        // Given
        String historyId = "test-history-123";
        String status = "a".repeat(1000);

        // When
        processStatusCache.updateStatus(historyId, status);

        // Then
        assertTrue(processStatusCache.containsStatus(historyId));
        assertEquals(status, processStatusCache.getStatus(historyId));
    }

    @Test
    @Timeout(10)
    void testShutdown() {
        // Given
        ProcessStatusCache cache = new ProcessStatusCache();
        String historyId = "test-history-123";
        String status = "RUNNING";

        cache.updateStatus(historyId, status);
        assertTrue(cache.containsStatus(historyId));

        // When
        cache.shutdown();

        // Then
        // Should not throw exception
        assertTrue(cache.containsStatus(historyId));
        assertEquals(status, cache.getStatus(historyId));
    }
}
