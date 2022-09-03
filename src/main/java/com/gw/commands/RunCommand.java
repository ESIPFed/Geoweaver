package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "run", subcommands = {RunWorkflowCommand.class, RunProcessCommand.class})
public class RunCommand implements Runnable {

    public void run() {
        
    }
    
}
