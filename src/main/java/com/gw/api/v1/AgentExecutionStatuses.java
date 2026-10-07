package com.gw.api.v1;

import com.gw.jpa.ExecutionStatus;
import java.util.Locale;
import java.util.Set;

/** Maps Geoweaver history indicators to stable Agent API status strings. */
public final class AgentExecutionStatuses {

  public static final Set<String> TERMINAL =
      Set.of(
          ExecutionStatus.DONE,
          ExecutionStatus.FAILED,
          ExecutionStatus.STOPPED,
          ExecutionStatus.SKIPPED);

  private AgentExecutionStatuses() {}

  public static String normalize(String raw) {
    if (raw == null || raw.isBlank()) {
      return ExecutionStatus.UNKOWN;
    }
    String t = raw.trim();
    if (ExecutionStatus.DONE.equalsIgnoreCase(t)) {
      return ExecutionStatus.DONE;
    }
    if (ExecutionStatus.FAILED.equalsIgnoreCase(t)) {
      return ExecutionStatus.FAILED;
    }
    if (ExecutionStatus.RUNNING.equalsIgnoreCase(t)) {
      return ExecutionStatus.RUNNING;
    }
    if (ExecutionStatus.STOPPED.equalsIgnoreCase(t)) {
      return ExecutionStatus.STOPPED;
    }
    if (ExecutionStatus.READY.equalsIgnoreCase(t)) {
      return ExecutionStatus.READY;
    }
    if (ExecutionStatus.SKIPPED.equalsIgnoreCase(t)) {
      return ExecutionStatus.SKIPPED;
    }
    if (ExecutionStatus.UNKOWN.equalsIgnoreCase(t) || "unknown".equals(t.toLowerCase(Locale.ROOT))) {
      return ExecutionStatus.UNKOWN;
    }
    return ExecutionStatus.UNKOWN;
  }

  public static boolean isTerminal(String status) {
    return TERMINAL.contains(normalize(status));
  }
}
