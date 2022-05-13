package com.gw.commands;

import org.springframework.stereotype.Component;

import picocli.CommandLine.Command;

// This is the main class for the command line interface.
// Register the commands with the CommandLine class as a subcommand.
@Component
@Command(subcommands = { PasswordResetCommand.class, RunCommand.class, ListCommand.class })
public class TopEntryCommand implements Runnable {

    public void run() {
        System.out.println("running top entry command");
    }
    
}
