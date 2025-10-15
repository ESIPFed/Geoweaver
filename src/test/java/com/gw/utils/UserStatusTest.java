package com.gw.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class UserStatusTest {

    @Test
    @Timeout(10)
    void testUserStatusValues() {
        // Test that all enum values exist
        assertNotNull(UserStatus.SUCCESS);
        assertNotNull(UserStatus.USER_ALREADY_EXISTS);
        assertNotNull(UserStatus.FAILURE);
    }

    @Test
    @Timeout(10)
    void testUserStatusValueOf() {
        // Test valueOf method
        assertEquals(UserStatus.SUCCESS, UserStatus.valueOf("SUCCESS"));
        assertEquals(UserStatus.USER_ALREADY_EXISTS, UserStatus.valueOf("USER_ALREADY_EXISTS"));
        assertEquals(UserStatus.FAILURE, UserStatus.valueOf("FAILURE"));
    }

    @Test
    @Timeout(10)
    void testUserStatusValuesArray() {
        // Test values() method
        UserStatus[] values = UserStatus.values();
        assertEquals(3, values.length);
        assertEquals(UserStatus.SUCCESS, values[0]);
        assertEquals(UserStatus.USER_ALREADY_EXISTS, values[1]);
        assertEquals(UserStatus.FAILURE, values[2]);
    }

    @Test
    @Timeout(10)
    void testUserStatusOrdinal() {
        // Test ordinal() method
        assertEquals(0, UserStatus.SUCCESS.ordinal());
        assertEquals(1, UserStatus.USER_ALREADY_EXISTS.ordinal());
        assertEquals(2, UserStatus.FAILURE.ordinal());
    }

    @Test
    @Timeout(10)
    void testUserStatusName() {
        // Test name() method
        assertEquals("SUCCESS", UserStatus.SUCCESS.name());
        assertEquals("USER_ALREADY_EXISTS", UserStatus.USER_ALREADY_EXISTS.name());
        assertEquals("FAILURE", UserStatus.FAILURE.name());
    }

    @Test
    @Timeout(10)
    void testUserStatusToString() {
        // Test toString() method
        assertEquals("SUCCESS", UserStatus.SUCCESS.toString());
        assertEquals("USER_ALREADY_EXISTS", UserStatus.USER_ALREADY_EXISTS.toString());
        assertEquals("FAILURE", UserStatus.FAILURE.toString());
    }

    @Test
    @Timeout(10)
    void testUserStatusEquality() {
        // Test equality
        assertEquals(UserStatus.SUCCESS, UserStatus.SUCCESS);
        assertEquals(UserStatus.USER_ALREADY_EXISTS, UserStatus.USER_ALREADY_EXISTS);
        assertEquals(UserStatus.FAILURE, UserStatus.FAILURE);
        
        assertNotEquals(UserStatus.SUCCESS, UserStatus.USER_ALREADY_EXISTS);
        assertNotEquals(UserStatus.SUCCESS, UserStatus.FAILURE);
        assertNotEquals(UserStatus.USER_ALREADY_EXISTS, UserStatus.FAILURE);
    }

    @Test
    @Timeout(10)
    void testUserStatusHashCode() {
        // Test hashCode() method
        assertEquals(UserStatus.SUCCESS.hashCode(), UserStatus.SUCCESS.hashCode());
        assertEquals(UserStatus.USER_ALREADY_EXISTS.hashCode(), UserStatus.USER_ALREADY_EXISTS.hashCode());
        assertEquals(UserStatus.FAILURE.hashCode(), UserStatus.FAILURE.hashCode());
    }

    @Test
    @Timeout(10)
    void testUserStatusCompareTo() {
        // Test compareTo() method
        assertTrue(UserStatus.SUCCESS.compareTo(UserStatus.USER_ALREADY_EXISTS) < 0);
        assertTrue(UserStatus.SUCCESS.compareTo(UserStatus.FAILURE) < 0);
        assertTrue(UserStatus.USER_ALREADY_EXISTS.compareTo(UserStatus.FAILURE) < 0);
        assertTrue(UserStatus.USER_ALREADY_EXISTS.compareTo(UserStatus.SUCCESS) > 0);
        assertTrue(UserStatus.FAILURE.compareTo(UserStatus.SUCCESS) > 0);
        assertTrue(UserStatus.FAILURE.compareTo(UserStatus.USER_ALREADY_EXISTS) > 0);
        
        assertEquals(0, UserStatus.SUCCESS.compareTo(UserStatus.SUCCESS));
        assertEquals(0, UserStatus.USER_ALREADY_EXISTS.compareTo(UserStatus.USER_ALREADY_EXISTS));
        assertEquals(0, UserStatus.FAILURE.compareTo(UserStatus.FAILURE));
    }
}
