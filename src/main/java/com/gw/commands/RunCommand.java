package com.gw.commands;

import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
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
        System.out.println("running run command with workflow id" + workflowId);
        // TODO: implement
        if (workflowZipOrPathToJson != null) {
            System.out.println("workflow zip or path to json: " + workflowZipOrPathToJson);
        }
        BaseTool bt = new BaseTool();
        WorkflowTool wt = new WorkflowTool();

        if (BaseTool.isNull(historyId)) {
            historyId = new RandomString(18).nextString();
        }
        

    }
    
}
