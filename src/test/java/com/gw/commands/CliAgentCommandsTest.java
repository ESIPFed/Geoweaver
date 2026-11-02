package com.gw.commands;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import picocli.CommandLine;

class CliAgentCommandsTest {

  @Test
  void detachedCopyNeverGetsPasswordsOnItsCommandLine() {
    String[] args = {
      "run", "workflow", "w1", "-h", "100001", "-p", "secret1", "--passwords=secret2", "-psecret3",
      "--password", "secret4", "--detach", "--json"
    };
    List<String> child = CliDetach.childArgs(args, "abcdef123456");
    assertThat(String.join(" ", child)).doesNotContain("secret").doesNotContain("--detach");
    assertThat(child).containsSubsequence("run", "workflow", "w1", "-h", "100001", "--json", "--run-id", "abcdef123456");
  }

  @Test
  void aRunIdGivenTwiceIsReplaced() {
    List<String> child = CliDetach.childArgs(new String[] {"run", "process", "p", "--run-id", "old123"}, "new456");
    assertThat(child).containsExactly("run", "process", "p", "--run-id", "new456");
  }

  @Test
  void runIdsAreSimpleWords() {
    assertThat(CliDetach.RUN_ID.matcher("abc123XYZ").matches()).isTrue();
    assertThat(CliDetach.RUN_ID.matcher("../etc").matches()).isFalse();
    assertThat(CliDetach.RUN_ID.matcher("a b").matches()).isFalse();
  }

  @Test
  void aStatusWordIsNotPrintedAsLog() {
    assertThat(RunInspectCommands.Log.isPlaceholder("Running")).isTrue();
    assertThat(RunInspectCommands.Log.isPlaceholder("  ")).isTrue();
    assertThat(RunInspectCommands.Log.isPlaceholder("line 1\n")).isFalse();
  }

  @Test
  void workflowStepsBecomeAChain() throws Exception {
    WorkflowCommand.Graph g = new WorkflowCommand.Graph();
    g.steps = new String[] {"p1", "p2", "p1"};
    g.read();
    assertThat(g.nodes.size()).isEqualTo(3);
    assertThat(g.nodes.get(0).get("id").asText()).startsWith("p1-");
    assertThat(g.nodes.get(2).get("id").asText()).startsWith("p1-").isNotEqualTo(g.nodes.get(0).get("id").asText());
    assertThat(g.edges.size()).isEqualTo(2);
    assertThat(g.edges.get(1).get("source").asText()).isEqualTo(g.nodes.get(1).get("id").asText());
  }

  @Test
  void errorsSetExitCodeOne() {
    CliOutput.fail(new ResponseStatusException(HttpStatus.NOT_FOUND, "Process not found"));
    assertThat(CliOutput.exitCode()).isEqualTo(1);
  }

  @Test
  void everyAgentCommandIsRegistered() {
    CommandLine cl = new CommandLine(new TopEntryCommand());
    assertThat(cl.getSubcommands()).containsKeys("process", "workflow", "run");
    assertThat(cl.getSubcommands().get("process").getSubcommands()).containsKeys("list", "show", "create", "update", "delete");
    assertThat(cl.getSubcommands().get("workflow").getSubcommands()).containsKeys("list", "show", "create", "update", "delete");
    assertThat(cl.getSubcommands().get("run").getSubcommands()).containsKeys("process", "workflow", "status", "log", "stop", "list");
  }
}
