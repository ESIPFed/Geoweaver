package com.gw.ssh;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for SSH classes
 */
public class SimpleSSHTest {

    @Test
    public void testSSHClassesExist() {
        // Test that SSH classes can be accessed
        assertNotNull(SSHAuthenticationException.class);
        assertNotNull(SSHAuthenticationProvider.class);
        assertNotNull(SSHCmdSessionOutput.class);
        assertNotNull(SSHLiveSessionOutput.class);
        assertNotNull(SSHSessionImpl.class);
        assertNotNull(RSAEncryptTool.class);
        assertNotNull(SecurityConfiguration.class);
        assertNotNull(ShellSocket.class);
    }
}
