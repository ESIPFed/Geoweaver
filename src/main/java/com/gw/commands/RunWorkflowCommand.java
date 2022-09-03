package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

import com.amazonaws.services.stepfunctions.model.ExecutionStatus;
import com.gw.jpa.History;
import com.gw.ssh.SSHSessionImpl;
import com.gw.tools.HistoryTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;

import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "workflow")
public class RunWorkflowCommand  implements Runnable {
    
    @Parameters(index = "0", description = "workflow id to run")
    String workflowId;

    @Option(names = { "-h", "--hosts" }, description = "hosts to run on")
    String[] hostStrings;

    @Option(names = { "-e", "--environments" }, description = "environments to run on")
    String[] envs;

    @Option(names = { "-f", "--workflowfile" }, description = "workflow package or path to workflow.json to run")
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

        System.out.println("The workflow execution has been successfully kicked off. \n" + response);

        System.out.println("Waiting for its finish...");

        HistoryTool ht = BeanTool.getBean(HistoryTool.class);

        History hist = ht.getHistoryById(historyId);;

        try {
        
            while(true){
        
                TimeUnit.SECONDS.sleep(2);
        
                hist = ht.getHistoryById(historyId);
        
                if(ht.checkIfEnd(hist)) break;
            
            }

        } catch (InterruptedException e) {
            
            e.printStackTrace();
        
        }


        System.out.println(String.format("Total time cost: %o seconds", 
                           BaseTool.calculateDuration(hist.getHistory_begin_time(), hist.getHistory_end_time())));
        
                           
        System.out.println(String.format("The workflow execution is over. Final status: %s.", hist.getIndicator()));

    }
    
}
