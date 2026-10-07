package com.gw.api.v1.dto;

/**
 * Start a workflow on localhost. {@code mode} is Geoweaver's execution mode: {@code one} (same
 * host for all steps) or {@code multiple} (per-node hosts — MVP still forces localhost).
 */
public record WorkflowRunRequest(String mode, String hostPassword, String clientRunKey) {}
