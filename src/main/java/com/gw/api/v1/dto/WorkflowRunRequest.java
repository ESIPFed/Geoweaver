package com.gw.api.v1.dto;

import java.util.Map;

/**
 * Start a workflow. All steps run on {@code hostId} (default localhost {@code 100001}) unless
 * {@code nodeHosts} assigns a host/environment to individual node ids. {@code hostPasswords} maps
 * each remote host id to its SSH password (used for this run only, never stored);
 * {@code hostPassword} is the password for {@code hostId}. {@code mode} is derived and only kept
 * for compatibility ({@code one} or {@code multiple}).
 */
public record WorkflowRunRequest(
    String mode,
    String hostPassword,
    String clientRunKey,
    String hostId,
    String envId,
    Map<String, NodeHost> nodeHosts,
    Map<String, String> hostPasswords) {

  /** Host (and optional Python environment) for one workflow node. */
  public record NodeHost(String hostId, String envId) {}

  public static WorkflowRunRequest empty() {
    return new WorkflowRunRequest(null, null, null, null, null, null, null);
  }
}
