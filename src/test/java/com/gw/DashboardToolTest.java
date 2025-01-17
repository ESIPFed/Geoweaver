package com.gw;

import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.jpa.GWProcess;
import com.gw.jpa.Host;
import com.gw.tools.DashboardTool;
import com.gw.utils.BaseTool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@Disabled("Skipping this test for now")
public class DashboardToolTest {

    @InjectMocks
    private DashboardTool dashboardTool;

    @Mock
    private HostRepository hostRepository;

    @Mock
    private WorkflowRepository workflowRepository;

    @Mock
    private ProcessRepository processRepository;

    @Mock
    private HistoryRepository historyRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private BaseTool baseTool;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void testGetAllProcessTimeCosts() {
        // Arrange
        Object[] failedProcess1 = {new Date(), new Date()};
        Object[] failedProcess2 = {null, null};
        Object[] successProcess1 = {new Date(), new Date()};
        Object[] successProcess2 = {null, null};
        List<Object[]> failedProcesses = Arrays.asList(failedProcess1, failedProcess2);
        List<Object[]> successProcesses = Arrays.asList(successProcess1, successProcess2);
        when(historyRepository.findFailedProcess()).thenReturn(failedProcesses);
        when(historyRepository.findSuccessProcess()).thenReturn(successProcesses);
        when(baseTool.parseSQLDateStr(anyString())).thenReturn(new Date());
        
        // Act
        int[] costs = dashboardTool.getAllProcessTimeCosts();
        
        // Assert
        assertEquals(4, costs.length);
        assertEquals(0, costs[0]);
        assertEquals(-1, costs[1]);
        assertEquals(0, costs[2]);
        assertEquals(-1, costs[3]);
    }

    @Test
    void testGetAllProcessTimeCostsJSON() {
        // Arrange
        when(dashboardTool.getAllProcessTimeCosts()).thenReturn(new int[] {0, -1, 0, -1});
        
        // Act
        String json = dashboardTool.getAllProcessTimeCostsJSON();
        
        // Assert
        assertEquals("[0,-1,0,-1]", json);
    }

    @Test
    void testGetJSON() {
        // Arrange
        when(processRepository.count()).thenReturn(5L);
        when(historyRepository.count()).thenReturn(10L);
        when(hostRepository.count()).thenReturn(3L);
        when(workflowRepository.count()).thenReturn(4L);
        when(environmentRepository.count()).thenReturn(2L);
        when(processRepository.findShellProcess()).thenReturn(Arrays.asList(new GWProcess(), new GWProcess()));
        when(processRepository.findNotebookProcess()).thenReturn(Arrays.asList(new GWProcess()));
        when(processRepository.findPythonProcess()).thenReturn(Arrays.asList(new GWProcess(), new GWProcess()));
        when(processRepository.findBuiltinProcess()).thenReturn(Arrays.asList(new GWProcess()));
        when(hostRepository.findSSHHosts()).thenReturn(Arrays.asList(new Host()));
        when(hostRepository.findJupyterNotebookHosts()).thenReturn(Arrays.asList(new Host(), new Host()));
        when(hostRepository.findJupyterLabHosts()).thenReturn(Arrays.asList(new Host()));
        when(hostRepository.findJupyterHubHosts()).thenReturn(Arrays.asList(new Host()));
        when(hostRepository.findGEEHosts()).thenReturn(Arrays.asList(new Host()));
        when(historyRepository.findRunningProcess()).thenReturn(Arrays.asList(new Object[] { }, new Object[] { }));
        when(historyRepository.findFailedProcess()).thenReturn(Arrays.asList(new Object[] { }, new Object[] { }));
        when(historyRepository.findSuccessProcess()).thenReturn(Arrays.asList(new Object[] { }, new Object[] { }));
        when(historyRepository.findRunningWorkflow()).thenReturn(Arrays.asList(new Object[] { }, new Object[] { }));
        when(historyRepository.findFailedWorkflow()).thenReturn(Arrays.asList(new Object[] { }, new Object[] { }));
        when(historyRepository.findSuccessWorkflow()).thenReturn(Arrays.asList(new Object[] { }, new Object[] { }));
        when(dashboardTool.getAllProcessTimeCostsJSON()).thenReturn("[0,1,2,3]");
        
        // Act
        String json = dashboardTool.getJSON();
        
        // Assert
        assertEquals("{\"process_num\":5,\"history_num\":10,\"host_num\":3,\"workflow_num\":4,\"environment_num\":2,\"process_shell_num\":2,\"process_notebook_num\":1,\"process_python_num\":2,\"process_builtin_num\":1,\"host_ssh_num\":1,\"host_jupyter_num\":2,\"host_jupyterlab_num\":1,\"host_jupyterhub_num\":1,\"host_gee_num\":1,\"running_process_num\":2,\"failed_process_num\":1,\"success_process_num\":2,\"running_workflow_num\":1,\"failed_workflow_num\":2,\"success_workflow_num\":1,\"time_costs\": [0,1,2,3] }", json);
    }

}


