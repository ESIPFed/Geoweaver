package com.gw.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import static org.junit.jupiter.api.Assertions.*;

class BeanConfigTest {

    @Test
    @Timeout(10)
    void testBeanConfigInstantiation() {
        // Given & When
        BeanConfig beanConfig = new BeanConfig();

        // Then
        assertNotNull(beanConfig);
    }

    @Test
    @Timeout(10)
    void testBeanConfigMultipleInstances() {
        // Given & When
        BeanConfig beanConfig1 = new BeanConfig();
        BeanConfig beanConfig2 = new BeanConfig();

        // Then
        assertNotNull(beanConfig1);
        assertNotNull(beanConfig2);
        assertNotSame(beanConfig1, beanConfig2);
    }

    @Test
    @Timeout(10)
    void testBeanConfigClass() {
        // Given
        BeanConfig beanConfig = new BeanConfig();

        // When
        Class<?> clazz = beanConfig.getClass();

        // Then
        assertEquals(BeanConfig.class, clazz);
        assertEquals("com.gw.utils.BeanConfig", clazz.getName());
    }

    @Test
    @Timeout(10)
    void testBeanConfigToString() {
        // Given
        BeanConfig beanConfig = new BeanConfig();

        // When
        String toString = beanConfig.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("BeanConfig"));
    }

    @Test
    @Timeout(10)
    void testBeanConfigHashCode() {
        // Given
        BeanConfig beanConfig1 = new BeanConfig();
        BeanConfig beanConfig2 = new BeanConfig();

        // When
        int hashCode1 = beanConfig1.hashCode();
        int hashCode2 = beanConfig2.hashCode();

        // Then
        assertTrue(hashCode1 != 0);
        assertTrue(hashCode2 != 0);
        // Different instances should have different hash codes
        assertNotEquals(hashCode1, hashCode2);
    }

    @Test
    @Timeout(10)
    void testBeanConfigEquals() {
        // Given
        BeanConfig beanConfig1 = new BeanConfig();
        BeanConfig beanConfig2 = new BeanConfig();
        BeanConfig beanConfig3 = beanConfig1;

        // When & Then
        assertEquals(beanConfig1, beanConfig1);
        assertEquals(beanConfig1, beanConfig3);
        assertNotEquals(beanConfig1, beanConfig2);
        assertNotEquals(beanConfig1, null);
        assertNotEquals(beanConfig1, "not a BeanConfig");
    }
}
