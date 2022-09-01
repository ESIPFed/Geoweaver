package com.gw.commands;

import com.gw.ssh.SSHSessionImpl;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "run")
public class RunCommand implements Runnable {

    @Parameters(index = "0", description = "workflow id to run")
    String workflowId;

    @Option(names = { "-h", "--hosts" }, description = "hosts to run on")
    String[] hostStrings;

    @Option(names = { "-e", "--environments" }, description = "environments to run on")
    String[] envs;

    @Option(names = { "-w", "--workflow" }, description = "workflow package or path to workflow.json to run")
    String workflowZipOrPathToJson;

    @Option(names = { "--history" }, description = "workflow history id to run", required = false, defaultValue = "")
    String historyId;

    public void run() {
        
        System.out.println("running run command with workflow id" + workflowId + "\n");
        
        if (workflowZipOrPathToJson != null) 
        
            System.out.println("workflow zip or path to json: " + workflowZipOrPathToJson);

        WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);

        if (BaseTool.isNull(historyId)) historyId = new RandomString(18).nextString();

        if(BaseTool.isNull(envs)) envs = new String[]{"default_option"};

        if(BaseTool.isNull(hostStrings)) hostStrings = new String[]{"10001"};

        String response = wt.execute(historyId, workflowId, "one", hostStrings, 
                                    new String[]{"123456"}, envs, "xxxxxxxxxx");

        System.out.println(response);

    }
    
}
