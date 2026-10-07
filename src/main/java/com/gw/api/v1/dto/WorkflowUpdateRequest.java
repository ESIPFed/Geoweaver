package com.gw.api.v1.dto;

public record WorkflowUpdateRequest(String name, String nodes, String edges, String description) {}
