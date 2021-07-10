package com.gw.tools;

import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardTool {

    @Autowired
	HostRepository hostrepository;

    @Autowired
	WorkflowRepository workflowrepository;

    @Autowired
	ProcessRepository processrepository;
	
	@Autowired
	HistoryRepository historyrepository;

    @Autowired
    EnvironmentRepository environmentrepository;

    public String getJSON(){

        int process_num = ((Long)processrepository.count()).intValue();

        int history_num = ((Long)historyrepository.count()).intValue();

        int host_num = ((Long)hostrepository.count()).intValue();

        int workflow_num = ((Long)workflowrepository.count()).intValue();

        int environment_num = ((Long)environmentrepository.count()).intValue();

        int process_shell_num =  processrepository.findShellProcess().size();

        int process_notebook_num = processrepository.findNotebookProcess().size();

        int process_python_num = processrepository.findPythonProcess().size();

        int process_builtin_num = processrepository.findBuiltinProcess().size();

        int host_ssh_num = hostrepository.findSSHHosts().size();

        int host_jupyter_num = hostrepository.findJupyterNotebookHosts().size();

        int host_jupyterlab_num = hostrepository.findJupyterLabHosts().size();

        int host_jupyterhub_num = hostrepository.findJupyterHubHosts().size();

        int host_gee_num = hostrepository.findGEEHosts().size();

        int running_process_num = historyrepository.findRunningProcess().size();

        int failed_process_num = historyrepository.findFailedProcess().size();

        int success_process_num = historyrepository.findSuccessProcess().size();

        int running_workflow_num = historyrepository.findRunningWorkflow().size();

        int failed_workflow_num = historyrepository.findFailedWorkflow().size();

        int success_workflow_num = historyrepository.findSuccessWorkflow().size();

        StringBuffer jsonbuf = new StringBuffer("{ \"process_num\":").append(process_num).append(",");

        jsonbuf.append("\"history_num\":").append(history_num).append(",");

        jsonbuf.append("\"host_num\":").append(host_num).append(",");

        jsonbuf.append("\"workflow_num\":").append(workflow_num).append(",");

        jsonbuf.append("\"environment_num\":").append(environment_num).append(",");

        jsonbuf.append("\"process_shell_num\":").append(process_shell_num).append(",");

        jsonbuf.append("\"process_notebook_num\":").append(process_notebook_num).append(",");

        jsonbuf.append("\"process_python_num\":").append(process_python_num).append(",");

        jsonbuf.append("\"process_builtin_num\":").append(process_builtin_num).append(",");

        jsonbuf.append("\"host_ssh_num\":").append(host_ssh_num).append(",");

        jsonbuf.append("\"host_jupyter_num\":").append(host_jupyter_num).append(",");

        jsonbuf.append("\"host_jupyterlab_num\":").append(host_jupyterlab_num).append(",");

        jsonbuf.append("\"host_jupyterhub_num\":").append(host_jupyterhub_num).append(",");

        jsonbuf.append("\"host_gee_num\":").append(host_gee_num).append(",");

        jsonbuf.append("\"running_process_num\":").append(running_process_num).append(",");

        jsonbuf.append("\"failed_process_num\":").append(failed_process_num).append(",");

        jsonbuf.append("\"success_process_num\":").append(success_process_num).append(",");

        jsonbuf.append("\"running_workflow_num\":").append(running_workflow_num).append(",");

        jsonbuf.append("\"failed_workflow_num\":").append(failed_workflow_num).append(",");

        jsonbuf.append("\"success_workflow_num\":").append(success_workflow_num).append("}");
        
        return jsonbuf.toString();

    }
    
}
