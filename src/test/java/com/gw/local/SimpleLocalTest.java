package com.gw.local;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for local session classes
 */
public class SimpleLocalTest {

    @Test
    public void testLocalClassesExist() {
        // Test that local classes can be accessed
        assertNotNull(LocalSessionNixImpl.class);
        assertNotNull(LocalSessionWinImpl.class);
        assertNotNull(LocalSessionOutput.class);
    }
}
