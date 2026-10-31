package com.gw.api.v1.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;

/**
 * Run (history) detail. For workflow runs {@code workflowRun} is true and {@code steps} lists each
 * node's child run; for process runs {@code steps} is omitted.
 */
public record RunResponse(
    String historyId,
    String processId,
    String hostId,
    String status,
    String rawStatus,
    boolean terminal,
    String beginTime,
    String endTime,
    String log,
    boolean truncated,
    boolean workflowRun,
    @JsonInclude(JsonInclude.Include.NON_NULL) List<RunStep> steps,
    @JsonInclude(JsonInclude.Include.NON_NULL) String notes) {}
