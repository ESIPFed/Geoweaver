package com.gw.commands;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.HelpCommand; // Default help subcommand, displays help for all commands

// This is the main class for the command line interface.
// Register the commands with the CommandLine class as a subcommand.
@Component
@Command(
    subcommands = {
      HistoryCommand.class,
      PasswordResetCommand.class,
      RunCommand.class,
      ListCommand.class,
      DetailCommand.class,
      HelpCommand.class,
      ImportCommand.class,
      ExportCommand.class,
      H2CompatibilityChecker.class,
      AgentTokenCommand.class,
      DbCompactCommand.class,
      ProcessCommand.class,
      WorkflowCommand.class
    },
    footer = {
      "",
      "Add --json to any command to get only JSON on standard output (log lines go to standard error).",
      "See docs/ssh-and-gui.md for using these commands from an SSH session or a coding agent."
    })
public class TopEntryCommand implements Runnable {

  Logger logger = Logger.getLogger(this.getClass());

  public void run() {

    logger.debug("should print out all supported commands");
  }
}
