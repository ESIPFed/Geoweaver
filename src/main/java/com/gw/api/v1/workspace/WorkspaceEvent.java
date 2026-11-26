package com.gw.api.v1.workspace;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Small JSON notice that a process or workflow inventory object changed. Never includes code bodies
 * or secrets. {@code revision} is a monotonically increasing workspace clock for client sync;
 * {@code status} and {@code target} are set for run events only.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record WorkspaceEvent(
    String event,
    String objectKind,
    String objectId,
    String objectName,
    String workflowId,
    String actor,
    String tokenFingerprint,
    String at,
    Long revision,
    String status,
    String target) {

  public static WorkspaceEvent of(
      String event,
      String objectKind,
      String objectId,
      String objectName,
      String workflowId,
      String actor,
      String tokenFingerprint,
      long revision) {
    return new WorkspaceEvent(
        event,
        objectKind,
        objectId,
        objectName,
        workflowId,
        actor,
        tokenFingerprint,
        Instant.now().toString(),
        revision,
        null,
        null);
  }

  /**
   * A run started, changed status or ended, in this or another Geoweaver on the same database.
   * {@code objectKind} is "run"; {@code objectId} the run (history) id; {@code target} the process
   * or workflow id and {@code objectName} its name; {@code workflowId} the workflow the run
   * belongs to, if any.
   */
  public static WorkspaceEvent run(
      String event, String historyId, String target, String name, String workflowId, String status) {
    return new WorkspaceEvent(
        event, "run", historyId, name, workflowId, "run", null, Instant.now().toString(), null, status, target);
  }

  /** The same event with another revision (used when passing it on to this server's browsers). */
  public WorkspaceEvent withRevision(long rev) {
    return new WorkspaceEvent(
        event, objectKind, objectId, objectName, workflowId, actor, tokenFingerprint, at, rev, status, target);
  }

  public static WorkspaceEvent agent(
      String event,
      String objectKind,
      String objectId,
      String objectName,
      String workflowId,
      String tokenFingerprint,
      long revision) {
    return of(
        event, objectKind, objectId, objectName, workflowId, "agent", tokenFingerprint, revision);
  }

  public static WorkspaceEvent human(
      String event,
      String objectKind,
      String objectId,
      String objectName,
      String workflowId,
      long revision) {
    return of(event, objectKind, objectId, objectName, workflowId, "human", null, revision);
  }
}
