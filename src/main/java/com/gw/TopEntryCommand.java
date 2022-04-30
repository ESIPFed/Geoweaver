package com.gw;

import com.gw.commands.PasswordResetCommand;
import com.gw.commands.RunCommand;

import picocli.CommandLine.Command;

// This is the main class for the command line interface.
// Register the commands with the CommandLine class as a subcommand.
@Command(subcommands = { PasswordResetCommand.class, RunCommand.class })
public class TopEntryCommand implements Runnable {

    public void run() {
        System.out.println("running top entry command");
    }
    
}
