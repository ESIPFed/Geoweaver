package com.gw.commands;

import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;

@Component
@Command(
    name = "run",
    subcommands = {
      RunWorkflowCommand.class,
      RunProcessCommand.class,
      RunInspectCommands.Status.class,
      RunInspectCommands.Log.class,
      RunInspectCommands.Stop.class,
      RunInspectCommands.ListRuns.class
    },
    description = "Run a process or workflow, and look at, follow or stop runs")
public class RunCommand implements Runnable {

  public void run() {}
}
