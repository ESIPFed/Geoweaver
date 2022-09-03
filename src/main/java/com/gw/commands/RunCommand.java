package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "run", subcommands = {RunWorkflowCommand.class, RunProcessCommand.class})
public class RunCommand implements Runnable {

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
