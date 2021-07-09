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

        return null;

    }
    
}
