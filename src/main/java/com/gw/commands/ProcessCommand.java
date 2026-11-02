package com.gw.commands;

import com.gw.api.v1.dto.ProcessCreateRequest;
import com.gw.api.v1.dto.ProcessUpdateRequest;
import com.gw.api.v1.service.AgentProcessService;
import com.gw.utils.BeanTool;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(
    name = "process",
    description = "Create, show, change or delete processes (JSON output, same as the Agent API)",
    subcommands = {
      ProcessCommand.ListCmd.class,
      ProcessCommand.Show.class,
      ProcessCommand.Create.class,
      ProcessCommand.Update.class,
      ProcessCommand.Delete.class
    })
public class ProcessCommand implements Runnable {

  public void run() {
    new picocli.CommandLine(this).usage(System.err);
  }

  static AgentProcessService service() {
    return BeanTool.getBean(AgentProcessService.class);
  }

  @Command(name = "list", description = "List processes")
  static class ListCmd implements Runnable {
    @Option(names = "--query", description = "only names containing this text")
    String query;

    public void run() {
      CliAgent.run(() -> service().listProcesses(query));
    }
  }

  @Command(name = "show", description = "Show a process with its code")
  static class Show implements Runnable {
    @Parameters(index = "0", description = "process id")
    String id;

    public void run() {
      CliAgent.run(() -> service().getProcess(id));
    }
  }

  @Command(name = "create", description = "Create a process; prints it with its new id")
  static class Create implements Runnable {
    @Option(names = "--name", required = true)
    String name;

    @Option(names = "--lang", description = "shell, python or builtin", defaultValue = "shell")
    String lang;

    @Option(names = "--code", description = "the code")
    String code;

    @Option(names = "--code-file", description = "read the code from this file, or - for standard input")
    String codeFile;

    @Option(names = "--description")
    String description;

    public void run() {
      CliAgent.run(
          () ->
              service()
                  .createProcess(
                      new ProcessCreateRequest(name, lang, CliAgent.code(code, codeFile), description)));
    }
  }

  @Command(name = "update", description = "Change a process; options not given stay as they are")
  static class Update implements Runnable {
    @Parameters(index = "0", description = "process id")
    String id;

    @Option(names = "--name")
    String name;

    @Option(names = "--lang")
    String lang;

    @Option(names = "--code")
    String code;

    @Option(names = "--code-file", description = "read the code from this file, or - for standard input")
    String codeFile;

    @Option(names = "--description")
    String description;

    public void run() {
      CliAgent.run(
          () ->
              service()
                  .updateProcess(
                      id,
                      new ProcessUpdateRequest(name, lang, CliAgent.code(code, codeFile), description)));
    }
  }

  @Command(name = "delete", description = "Delete a process (refused while a workflow uses it)")
  static class Delete implements Runnable {
    @Parameters(index = "0", description = "process id")
    String id;

    public void run() {
      CliAgent.run(() -> service().deleteProcess(id));
    }
  }
}
