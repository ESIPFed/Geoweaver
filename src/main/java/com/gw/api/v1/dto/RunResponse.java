package com.gw.api.v1.dto;

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
    boolean truncated) {}
