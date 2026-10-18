package com.gw.api.v1.workspace;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Small JSON notice that a process or workflow inventory object changed. Never includes code bodies
 * or secrets. {@code revision} is a monotonically increasing workspace clock for client sync.
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
    Long revision) {

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
        revision);
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
