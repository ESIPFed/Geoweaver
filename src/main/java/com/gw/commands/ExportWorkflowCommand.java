package com.gw.commands;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.springframework.stereotype.Component;

import com.gw.tools.WorkflowTool;
import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "workflow")
public class ExportWorkflowCommand implements Runnable {

    @Option(names = { "--mode" }, description = 
        "exportation model options: \n"+
        "   1 - workflow only \n    2 - workflow with process code \n   3 - workflow with process code and only good history \n 4 - workflow with process code and all the history."+
        "default option is 4.")
    int export_mode;

    @Parameters(index = "0", description = "Geoweaver workflow ID")
    String workflow_id;

    @Parameters(index = "1", description = "target file path to save the workflow zip")
    String target_file_path;

    @Override
    public void run() {
        
        try{

            WorkflowTool wt = BeanTool.getBean(WorkflowTool.class);

            BaseTool bt = BeanTool.getBean(BaseTool.class);

            wt.download(workflow_id, wt.getExportModeById(export_mode));

            String defaultsavefilepath = bt.getFileTransferFolder() + workflow_id + ".zip";
            
            Files.copy(new File(defaultsavefilepath).toPath(), new File(target_file_path).toPath(), StandardCopyOption.REPLACE_EXISTING);

            System.out.println(String.format("Workflow %s has been exported to file: %s", workflow_id, target_file_path));

        }catch(Exception e){

            System.err.println(String.format("Fail to export workflow %s. Reason: %s", 
                workflow_id, e.getLocalizedMessage()));

        }

    }

}
