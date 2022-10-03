package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

@Component
@Command(name = "export", subcommands = {ExportWorkflowCommand.class, ExportProcessCommand.class})
public class ExportCommand  implements Runnable {

    @Override
    public void run() {

        
    }

}
