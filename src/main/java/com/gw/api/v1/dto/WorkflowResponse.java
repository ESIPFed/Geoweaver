package com.gw.api.v1.dto;

public record WorkflowResponse(
    String id, String name, String description, String owner, String nodes, String edges) {}
