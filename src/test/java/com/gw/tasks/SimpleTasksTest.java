package com.gw.tasks;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Simple tests for task classes
 */
public class SimpleTasksTest {

    @Test
    public void testTaskClassesExist() {
        // Test that task classes can be accessed
        assertNotNull(Task.class);
        assertNotNull(TaskManager.class);
        assertNotNull(TaskSocket.class);
        assertNotNull(GeoweaverProcessTask.class);
        assertNotNull(GeoweaverWorkflowTask.class);
        assertNotNull(RunningTaskObserver.class);
        assertNotNull(WaitingTaskObserver.class);
    }
}
