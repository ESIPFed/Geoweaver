package com.gw.commands;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gw.api.v1.dto.WorkflowCreateRequest;
import com.gw.api.v1.dto.WorkflowUpdateRequest;
import com.gw.api.v1.service.AgentWorkflowService;
import com.gw.utils.BeanTool;
import com.gw.utils.RandomString;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Component
@Command(
    name = "workflow",
    description = "Create, show, change or delete workflows (JSON output, same as the Agent API)",
    subcommands = {
      WorkflowCommand.ListCmd.class,
      WorkflowCommand.Show.class,
      WorkflowCommand.Create.class,
      WorkflowCommand.Update.class,
      WorkflowCommand.Delete.class
    })
public class WorkflowCommand implements Runnable {

  public void run() {
    new picocli.CommandLine(this).usage(System.err);
  }

  static AgentWorkflowService service() {
    return BeanTool.getBean(AgentWorkflowService.class);
  }

  /** The graph: from --steps (a chain of processes, in order) or --file (nodes and edges JSON). */
  static class Graph {
    @Option(
        names = "--steps",
        split = ",",
        description = "process ids, run one after another (a process may appear more than once)")
    String[] steps;

    @Option(
        names = "--file",
        description = "JSON file with \"nodes\" and \"edges\" as in the Agent API, or - for standard input")
    String file;

    JsonNode nodes;
    JsonNode edges;

    boolean given() {
      return steps != null || file != null;
    }

    void read() throws IOException {
      if (file != null) {
        String text =
            "-".equals(file) ? new String(System.in.readAllBytes()) : Files.readString(Path.of(file));
        JsonNode doc = CliOutput.mapper().readTree(text);
        nodes = doc.get("nodes");
        edges = doc.get("edges");
        return;
      }
      if (steps == null) return;
      JsonNodeFactory f = JsonNodeFactory.instance;
      ArrayNode n = f.arrayNode();
      ArrayNode e = f.arrayNode();
      String prev = null;
      for (String p : steps) {
        String id = p.trim() + "-" + new RandomString(5).nextString();
        n.addObject().put("id", id);
        if (prev != null) {
          ObjectNode edge = e.addObject();
          edge.put("source", prev);
          edge.put("target", id);
        }
        prev = id;
      }
      nodes = n;
      edges = e;
    }
  }

  @Command(name = "list", description = "List workflows")
  static class ListCmd implements Runnable {
    @Option(names = "--query", description = "only names containing this text")
    String query;

    public void run() {
      CliAgent.run(() -> service().listWorkflows(query));
    }
  }

  @Command(name = "show", description = "Show a workflow with its steps")
  static class Show implements Runnable {
    @Parameters(index = "0", description = "workflow id")
    String id;

    public void run() {
      CliAgent.run(() -> service().getWorkflow(id));
    }
  }

  @Command(name = "create", description = "Create a workflow; prints it with its new id")
  static class Create implements Runnable {
    @Option(names = "--name", required = true)
    String name;

    @Option(names = "--description")
    String description;

    @picocli.CommandLine.Mixin Graph graph;

    public void run() {
      CliAgent.run(
          () -> {
            graph.read();
            return service()
                .createWorkflow(
                    new WorkflowCreateRequest(name, graph.nodes, graph.edges, description));
          });
    }
  }

  @Command(name = "update", description = "Change a workflow; options not given stay as they are")
  static class Update implements Runnable {
    @Parameters(index = "0", description = "workflow id")
    String id;

    @Option(names = "--name")
    String name;

    @Option(names = "--description")
    String description;

    @picocli.CommandLine.Mixin Graph graph;

    public void run() {
      CliAgent.run(
          () -> {
            graph.read();
            JsonNode nodes = graph.nodes;
            JsonNode edges = graph.edges;
            return service().updateWorkflow(id, new WorkflowUpdateRequest(name, nodes, edges, description));
          });
    }
  }

  @Command(name = "delete", description = "Delete a workflow (its run history is kept)")
  static class Delete implements Runnable {
    @Parameters(index = "0", description = "workflow id")
    String id;

    public void run() {
      CliAgent.run(() -> service().deleteWorkflow(id));
    }
  }
}
