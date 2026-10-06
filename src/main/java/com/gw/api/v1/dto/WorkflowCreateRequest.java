package com.gw.api.v1.dto;

/** Create a workflow from Geoweaver graph JSON (nodes + edges). */
public record WorkflowCreateRequest(String name, String nodes, String edges, String description) {}
