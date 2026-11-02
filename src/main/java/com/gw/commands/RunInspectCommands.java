package com.gw.commands;

import com.gw.api.v1.dto.RunResponse;
import com.gw.api.v1.dto.RunStep;
import com.gw.api.v1.service.AgentHistoryService;
import com.gw.api.v1.service.AgentRunSupport;
import com.gw.database.HistoryRepository;
import com.gw.jpa.History;
import com.gw.utils.BeanTool;
import com.gw.utils.RunLogStore;
import java.util.HashMap;
import java.util.Map;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

/** Look at, follow and stop runs, wherever they were started (browser, command line, Agent API). */
final class RunInspectCommands {

  private RunInspectCommands() {}

  static AgentRunSupport runs() {
    return BeanTool.getBean(AgentRunSupport.class);
  }

  /** Waits up to {@code seconds} for the run to end; 0 returns at once. */
  static RunResponse await(String runId, int logChars, int seconds, boolean stepLogs) {
    long deadline = System.currentTimeMillis() + seconds * 1000L;
    while (true) {
      int left = (int) Math.max(0, (deadline - System.currentTimeMillis() + 999) / 1000);
      RunResponse r =
          runs().getRun(runId, logChars, Math.min(left, AgentRunSupport.MAX_WAIT_SECONDS), stepLogs);
      if (r.terminal() || left <= AgentRunSupport.MAX_WAIT_SECONDS) return r;
    }
  }

  @Command(
      name = "status",
      description = "Show a run: status, the end of its log and, for workflows, each step")
  static class Status implements Runnable {
    @Parameters(index = "0", description = "run id (history id)")
    String runId;

    @Option(names = "--wait", description = "wait up to this many seconds for the run to end", defaultValue = "0")
    int wait;

    @Option(names = "--log-chars", description = "characters of log to include", defaultValue = "2000")
    int logChars;

    @Option(names = "--step-logs", description = "include each step's log (workflow runs)")
    boolean stepLogs;

    public void run() {
      CliAgent.run(
          () -> {
            RunResponse r = await(runId, logChars, Math.max(0, wait), stepLogs);
            if (CliOutput.json() && r.terminal() && !"Done".equals(r.status())) CliOutput.setExitCode(1);
            return r;
          });
    }
  }

  @Command(name = "log", description = "Print the full log of a run (all steps for a workflow run)")
  static class Log implements Runnable {
    @Parameters(index = "0", description = "run id (history id)")
    String runId;

    @Option(names = {"-f", "--follow"}, description = "keep printing new output until the run ends")
    boolean follow;

    public void run() {
      CliAgent.begin();
      try {
        print();
      } catch (Exception e) {
        CliOutput.fail(e);
      }
    }

    private void print() throws InterruptedException {
      HistoryRepository repo = BeanTool.getBean(HistoryRepository.class);
      Map<String, String> printed = new HashMap<>();
      Map<String, String> stepState = new HashMap<>();
      while (true) {
        RunResponse r = runs().getRun(runId, 0);
        if (r.workflowRun() && r.steps() != null) {
          for (RunStep s : r.steps()) {
            String key = s.historyId() == null ? s.nodeId() : s.historyId();
            if (!s.status().equals(stepState.put(key, s.status()))) {
              CliOutput.out().println("== step " + s.nodeId() + " : " + s.status());
            }
            if (s.historyId() != null) emit(repo, s.historyId(), printed);
          }
        } else {
          emit(repo, runId, printed);
        }
        CliOutput.out().flush();
        if (!follow || r.terminal()) {
          if (follow) CliOutput.out().println("== run " + runId + " ended: " + r.status());
          if (r.terminal() && !"Done".equals(r.status())) CliOutput.setExitCode(1);
          return;
        }
        Thread.sleep(2000);
      }
    }

    /** Prints what is new in a run's log since the last call. */
    static void emit(HistoryRepository repo, String id, Map<String, String> printed) {
      History h = repo.findById(id).orElse(null);
      if (h == null) return;
      String text = RunLogStore.readFull(id, h.getHistory_output());
      if (text == null || isPlaceholder(text)) return; // the run has written nothing yet
      String before = printed.getOrDefault(id, "");
      String add = text.startsWith(before) ? text.substring(before.length()) : text;
      if (!add.isEmpty()) {
        CliOutput.out().print(add);
        if (!add.endsWith("\n")) CliOutput.out().println();
      }
      printed.put(id, text);
    }

    /** Before a run writes output, its log holds only its status. */
    static boolean isPlaceholder(String text) {
      String t = text.trim();
      return t.isEmpty() || t.equals("Running") || t.equals("Ready") || t.equals("Pending");
    }
  }

  @Command(
      name = "stop",
      description = "Stop a run, also one started in a browser or by another command")
  static class Stop implements Runnable {
    @Parameters(index = "0", description = "run id (history id)")
    String runId;

    @Option(names = "--wait", description = "seconds to wait for the run to end", defaultValue = "10")
    int wait;

    public void run() {
      CliAgent.run(
          () -> {
            runs().stopRun(runId);
            return await(runId, 0, Math.max(0, wait), false);
          });
    }
  }

  @Command(name = "list", description = "Recent runs, newest first")
  static class ListRuns implements Runnable {
    @Option(names = "--status", description = "only runs with this status, for example Running or Failed")
    String status;

    @Option(names = "--limit", defaultValue = "20")
    int limit;

    public void run() {
      CliAgent.run(() -> BeanTool.getBean(AgentHistoryService.class).listRuns(limit, status, 0));
    }
  }
}
