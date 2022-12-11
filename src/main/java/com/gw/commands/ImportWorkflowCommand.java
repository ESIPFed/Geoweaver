package com.gw.commands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "workflow", description = "import a workflow from file")
public class ImportWorkflowCommand implements Runnable {
    
    @Parameters(index = "0", description = "Geoweaver workflow zip file path")
    String workflow_zip_file_path;
    
    public void run() {

        try{

            WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);
            BaseTool bt = BeanTool.getBean(BaseTool.class);

            //first, unzip the file
            File source_file = new File(workflow_zip_file_path);
            String filename = source_file.getName();
            Files.copy(source_file.toPath(),
                    new File(bt.getFileTransferFolder() + filename).toPath(), StandardCopyOption.REPLACE_EXISTING);
            
            //second, precheck the folder is valid geoweaver package
            String resp = wt.precheck(filename);
            System.out.println("Precheck: Done");
            ObjectMapper mapper = new ObjectMapper();
            Map<String, String> map = mapper.readValue(resp, Map.class);
            String wid = String.valueOf(map.get("id"));
            System.out.println(String.format("Workflow ID: %s", wid));

            //third, import the workflow and save to database
            resp = wt.saveWorkflowFromFolder(wid, filename);
            System.out.println("Successfully saved to database. Complete.");

        }catch(Exception e){

            System.err.println(String.format("Failed to import: %s. Reason: %s", 
                workflow_zip_file_path, e.getLocalizedMessage()));

        }
        
    }

}
