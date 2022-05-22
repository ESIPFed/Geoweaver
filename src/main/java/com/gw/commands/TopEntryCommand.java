package com.gw.commands;

import com.gw.utils.BaseTool;
import com.gw.utils.BeanTool;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

// This is the main class for the command line interface.
// Register the commands with the CommandLine class as a subcommand.
@Component
@Command(subcommands = { PasswordResetCommand.class, RunCommand.class, ListCommand.class })
public class TopEntryCommand implements Runnable {

    Logger logger = Logger.getLogger(this.getClass());

    public void run() {
        // BaseTool bt = BeanTool.getBean(BaseTool.class);
        // bt.printoutCallStack();
        logger.debug("should print out all supported commands");
    }
    
}
