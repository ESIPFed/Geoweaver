package com.gw.tools;

import com.gw.database.EnvironmentRepository;
import com.gw.database.HistoryRepository;
import com.gw.database.HostRepository;
import com.gw.database.ProcessRepository;
import com.gw.database.WorkflowRepository;
import com.gw.utils.BaseTool;
import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DashboardTool {

  @Autowired HostRepository hostrepository;

  @Autowired WorkflowRepository workflowrepository;

  @Autowired ProcessRepository processrepository;

  @Autowired HistoryRepository historyrepository;

  @Autowired EnvironmentRepository environmentrepository;

  @Autowired BaseTool bt;

  public int[] getAllProcessTimeCosts() {

    List failed_processes = historyrepository.findFailedProcess();

    List success_processes = historyrepository.findSuccessProcess();

    int[] costs = new int[failed_processes.size() + success_processes.size()];

    int num = 0;

    for (int i = 0; i < failed_processes.size(); i++) {

      Object[] cols = (Object[]) failed_processes.get(i);

      costs[num++] = calculateTimeCostMillis(cols);
    }

    for (int i = 0; i < success_processes.size(); i++) {

      Object[] cols = (Object[]) success_processes.get(i);

      costs[num++] = calculateTimeCostMillis(cols);
    }

    return costs;
  }

  private int calculateTimeCostMillis(Object[] cols) {

    if (cols[1] == null || cols[2] == null) {
      return -1;
    }

    Date begin_time = toHistoryDate(cols[1]);
    Date end_time = toHistoryDate(cols[2]);

    if (begin_time == null || end_time == null) {
      return -1;
    }

    return (int) Math.abs(end_time.getTime() - begin_time.getTime());
  }

  private Date toHistoryDate(Object value) {

    if (value instanceof Date dateValue) {
      return dateValue;
    }

    if (value instanceof java.sql.Timestamp timestamp) {
      return new Date(timestamp.getTime());
    }

    return bt.parseSQLDateStr(String.valueOf(value));
  }

  public String getAllProcessTimeCostsJSON() {

    int[] costs = getAllProcessTimeCosts();

    StringBuffer costbuf = new StringBuffer("[");

    for (int i = 0; i < costs.length; i++) {

      if (i != 0) costbuf.append(",");

      costbuf.append(costs[i]);
    }

    costbuf.append("]");

    return costbuf.toString();
  }

  public String getJSON() {

    int process_num = (int) processrepository.count();

    int history_num = (int) historyrepository.count();

    int host_num = (int) hostrepository.count();

    int workflow_num = (int) workflowrepository.count();

    int environment_num = (int) environmentrepository.count();

    int process_shell_num = processrepository.findShellProcess().size();

    int process_python_num = processrepository.findPythonProcess().size();

    int process_builtin_num = processrepository.findBuiltinProcess().size();

    int host_ssh_num = hostrepository.findSSHHosts().size();

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
    jsonbuf.append("\"process_python_num\":").append(process_python_num).append(",");

    jsonbuf.append("\"process_builtin_num\":").append(process_builtin_num).append(",");

    jsonbuf.append("\"host_ssh_num\":").append(host_ssh_num).append(",");
    jsonbuf.append("\"host_gee_num\":").append(host_gee_num).append(",");

    jsonbuf.append("\"running_process_num\":").append(running_process_num).append(",");

    jsonbuf.append("\"failed_process_num\":").append(failed_process_num).append(",");

    jsonbuf.append("\"success_process_num\":").append(success_process_num).append(",");

    jsonbuf.append("\"running_workflow_num\":").append(running_workflow_num).append(",");

    jsonbuf.append("\"failed_workflow_num\":").append(failed_workflow_num).append(",");

    jsonbuf.append("\"success_workflow_num\":").append(success_workflow_num).append(",");

    jsonbuf.append("\"time_costs\": ").append(getAllProcessTimeCostsJSON()).append(" }");

    return jsonbuf.toString();
  }
}
