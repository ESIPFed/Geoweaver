package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Component
@Command(name = "workflow", description = "import a workflow from file or folder")
public class ImportWorkflowCommand implements Runnable {


    @Parameters(index = "0", description = "Geoweaver workflow zip file path")
    String workflow_file_path;
    
    public void run() {
    
        
    
    }

}
