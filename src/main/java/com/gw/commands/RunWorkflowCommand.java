package com.gw.commands;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.FileUtils;
import org.json.simple.parser.ParseException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Option(names = { "-f", "--workflow-zip-file-path" }, description = "workflow package or path to workflow zip to run")
    String workflowZipPath;

    @Option(names = {"-d", "--workflow-folder-path"}, description = "geoweaver workflow folder path")
    String workflowFolderPath;

    @Option(names = { "-h", "--hosts" }, description = "hosts to run on")
    String[] hostStrings;

    @Option(names = { "-e", "--environments" }, description = "environments to run on")
    String[] envs;

    @Option(names = { "-p", "--passwords" }, description = "passwords to the target hosts")
    String[] passes;

    

    public void run() {

        System.out.printf("Running workflow %s%n", workflowId);
        
        if (workflowZipPath != null) 
        
            System.out.printf("workflow zip file: %s", workflowZipPath);

        WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);

        BaseTool bt = BeanTool.getBean(BaseTool.class);

        String historyId = new RandomString(18).nextString();

        if(BaseTool.isNull(envs)) envs = new String[]{"default_option"};

        if(BaseTool.isNull(hostStrings)) hostStrings = new String[]{"10001"};

        if (workflowFolderPath != null && workflowZipPath != null) {
            
            // if both zip location and folder location are passed - error out.
            System.err.printf("Error: Either pass workflow folder path `-d` or workflow zip path `-f`. Cannot use both.");
            
            System.exit(1);

        }

        if (workflowFolderPath != null) {

            // zip the folder into a zip file in gw workflow, import it into Geoweaver DB
            try{

                Path sourceDirectoryPath = Paths.get(workflowFolderPath).toAbsolutePath();
                Path destinationPath = Paths.get(bt.getFileTransferFolder() + workflowId + ".zip");
                if(destinationPath.toFile().exists()) destinationPath.toFile().delete();

                bt.zipFolder(sourceDirectoryPath.toString(), destinationPath.toString());
                System.out.printf("The folder is zipped into %s", destinationPath.toString());
                String filename = destinationPath.toFile().getName();
                
                //precheck the folder is valid geoweaver package
                String resp = wt.precheck(filename);
                System.out.println("Precheck: Done");
                ObjectMapper mapper = new ObjectMapper();
                Map<String, String> map = mapper.readValue(resp, Map.class);
                String wid = String.valueOf(map.get("id"));
                System.out.printf("Workflow ID: %s", wid);

                //import the workflow and save to database
                resp = wt.saveWorkflowFromFolder(wid, filename);
                System.out.println("Successfully saved to database. Complete.");

            }catch(Exception e){

                e.printStackTrace();

                throw new RuntimeException("Fail to import the folder into Geowaever"+e.getLocalizedMessage());

            }

            
        }

        // execute the workflow from DB

        String response = wt.execute(historyId, workflowId, "one", hostStrings,
                    passes, envs, "xxxxxxxxxx");

        System.out.printf("The workflow has been kicked off.\nHistory Id: %s%n", historyId);

        System.out.println("Waiting for it to finish");

        HistoryTool ht = BeanTool.getBean(HistoryTool.class);

        History hist = ht.getHistoryById(historyId);
        List<Map<String, Serializable>> mapper = new ArrayList<>();
        try {
        
            while(true){
        
                TimeUnit.SECONDS.sleep(2);

                hist = ht.getHistoryById(historyId);

                if(ht.checkIfEnd(hist)) break;
            
            }

        } catch (InterruptedException e) {
            
            e.printStackTrace();
        
        }

        if (workflowFolderPath != null) {

            try {

                // export the workflow from db into a new zip file in gw workspace folder, unzip it to the original folder specified by `-d`
                
                Path destinationPath = Paths.get(bt.getFileTransferFolder() + workflowId + ".zip");

                if(destinationPath.toFile().exists()) destinationPath.toFile().delete();

                wt.download(workflowId, wt.getExportModeById(4)); // default 4

                bt.unzip(destinationPath.toString(), workflowFolderPath);

            } catch (ParseException e) {

                e.printStackTrace();
                
            }  

        }


        System.out.printf("Total time cost: %o seconds%n",
                           BaseTool.calculateDuration(hist.getHistory_begin_time(), hist.getHistory_end_time()));
        
                           
        System.out.printf("Execution is over. Final status: %s.%n", hist.getIndicator());

    }
    
}
