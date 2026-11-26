package com.gw.commands;

import com.gw.api.v1.AgentApiAuthContext;
import com.gw.api.v1.workspace.WorkspaceEventBroadcaster;
import com.gw.api.v1.workspace.WorkspaceFeedWriter;
import com.gw.utils.BeanTool;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;

/** Shared set-up for commands that change or inspect Geoweaver data through the agent services. */
final class CliAgent {

  /** Shown as the actor of command-line changes in open browser pages and in the server log. */
  static final String ACTOR = "cli";

  private CliAgent() {}

  /** Changes made by this command are passed on to open Geoweaver pages on the same database. */
  static void begin() {
    AgentApiAuthContext.setTokenFingerprint(ACTOR);
    try {
      WorkspaceFeedWriter writer = BeanTool.getBean(WorkspaceFeedWriter.class);
      BeanTool.getBean(WorkspaceEventBroadcaster.class).setSharer(writer::share);
    } catch (RuntimeException e) {
      // pages refresh on their own later; never fail the command for this
    }
  }

  /** Runs {@code body} and prints its result; errors become {@code {"error": ...}} and exit 1. */
  static void run(Callable<Object> body) {
    begin();
    try {
      Object result = body.call();
      if (result != null) CliOutput.print(result);
    } catch (Exception e) {
      CliOutput.fail(e);
    }
  }

  /** Code given inline, or read from a file, or from standard input when the file is "-". */
  static String code(String inline, String file) throws IOException {
    if (file == null) return inline;
    if ("-".equals(file)) return new String(System.in.readAllBytes(), StandardCharsets.UTF_8);
    return Files.readString(Path.of(file), StandardCharsets.UTF_8);
  }
}
