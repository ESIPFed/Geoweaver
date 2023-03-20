package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "import", subcommands = {ImportWorkflowCommand.class, ImportProcessCommand.class}, 
    description = "Import workflow and process into Geoweaver database")
public class ImportCommand implements Runnable{


    public void run() {
        
    }
    


}
