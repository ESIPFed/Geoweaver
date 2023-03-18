package com.gw.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

import com.gw.jpa.History;
import com.gw.tools.HistoryTool;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.lang.constant.Constable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    @Option(names = {"-w", "--workflow-json-path"}, description = "geoweaver workflow.json path")
    String workflowJSONPath;

    public void run() {

        System.out.printf("Running workflow %s%n", workflowId);
        
        if (workflowZipOrPathToJson != null) 
        
            System.out.println("workflow zip or path to json: " + workflowZipOrPathToJson);

        WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);
        BaseTool bt = BeanTool.getBean(BaseTool.class);

        String historyId = new RandomString(18).nextString();

        if(BaseTool.isNull(envs)) envs = new String[]{"default_option"};

        if(BaseTool.isNull(hostStrings)) hostStrings = new String[]{"10001"};

        if (workflowJSONPath != null && workflowZipOrPathToJson != null) {
            // if zip location and workflow.json location are passed - error out.
            try {
                throw new Exception("Either pass workflow json or workflow zip path");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        if (workflowJSONPath != null) {
            Path sourceDirectoryPath = Paths.get(workflowJSONPath).toAbsolutePath();
            Path destinationPath = Paths.get(bt.getFileTransferFolder() + Paths.get(workflowJSONPath).getFileName());

            try {
                FileUtils.copyDirectory(sourceDirectoryPath.toFile(), destinationPath.toFile());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            wt.execute(historyId, String.valueOf(sourceDirectoryPath.getFileName()), "one", hostStrings,
                    passes, envs, "xxxxxxxxxx");
        } else {
            String response = wt.execute(historyId, workflowId, "one", hostStrings,
                    passes, envs, "xxxxxxxxxx");
        }

        System.out.printf("The workflow has been kicked off.\nHistory Id: %s%n", historyId);

        System.out.println("Waiting for it to finish");

        HistoryTool ht = BeanTool.getBean(HistoryTool.class);

        History hist = ht.getHistoryById(historyId);
        List<Map<java.lang.constant.Constable, Serializable>> mapper = new ArrayList<>();
        try {
        
            while(true){
        
                TimeUnit.SECONDS.sleep(2);

                hist = ht.getHistoryById(historyId);

                if (workflowJSONPath != null) {
                    Map<Constable, Serializable> mMap = new HashMap<>();
                    mMap.put("history_id", hist.getHistory_id());
                    mMap.put("history_input", hist.getHistory_input());
                    mMap.put("history_output", hist.getHistory_output());
                    mMap.put("history_begin_time", hist.getHistory_begin_time().getTime() / 1000);
                    mMap.put("history_end_time", hist.getHistory_end_time().getTime() / 1000);
                    mMap.put("history_notes", hist.getHistory_notes());
                    mMap.put("history_process", hist.getHistory_process());
                    mMap.put("host_id", hist.getHost_id());
                    mMap.put("indicator", hist.getIndicator());
                    mapper.add(mMap);
                }

                if(ht.checkIfEnd(hist)) break;
            
            }

        } catch (InterruptedException e) {
            
            e.printStackTrace();
        
        }

        if (workflowJSONPath != null) {
            String sourceDirectoryPath = Path.of(Paths.get(workflowJSONPath).toAbsolutePath() + "/history/" + historyId + ".json").toString();
            ObjectMapper m = new ObjectMapper();
            try {
                m.writeValue(new File(sourceDirectoryPath), mapper);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        System.out.printf("Total time cost: %o seconds%n",
                           BaseTool.calculateDuration(hist.getHistory_begin_time(), hist.getHistory_end_time()));
        
                           
        System.out.printf("Execution is over. Final status: %s.%n", hist.getIndicator());

    }
    
}
