package com.gw.api.v1.dto;

/** One workflow node's run inside a workflow history. {@code log} is null unless requested. */
public record RunStep(
    String nodeId,
    String processId,
    String historyId,
    String status,
    boolean terminal,
    String beginTime,
    String endTime,
    String log) {}
