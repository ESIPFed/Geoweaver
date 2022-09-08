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

    @Option(names = { "-f", "--workflowfile" }, description = "workflow package or path to workflow.json to run")
    String workflowZipOrPathToJson;

    @Option(names = { "-h", "--hosts" }, description = "hosts to run on")
    String[] hostStrings;

    @Option(names = { "-e", "--environments" }, description = "environments to run on")
    String[] envs;

    @Option(names = { "-p", "--passwords" }, description = "passwords to the target hosts")
    String[] passes;

    public void run() {

        System.out.println(String.format("Running workflow %s", workflowId));
        
        if (workflowZipOrPathToJson != null) 
        
            System.out.println("workflow zip or path to json: " + workflowZipOrPathToJson);

        WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);

        String historyId = new RandomString(18).nextString();

        if(BaseTool.isNull(envs)) envs = new String[]{"default_option"};

        if(BaseTool.isNull(hostStrings)) hostStrings = new String[]{"10001"};

        String response = wt.execute(historyId, workflowId, "one", hostStrings, 
                                    passes, envs, "xxxxxxxxxx");

        System.out.println(String.format("The workflow has been kicked off.\nHistory Id: %s", historyId));

        System.out.println("Waiting for it to finish");

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
        
                           
        System.out.println(String.format("Execution is over. Final status: %s.", hist.getIndicator()));

    }
    
}
