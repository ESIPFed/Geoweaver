package com.gw.api.v1.dto;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Create a workflow from Geoweaver graph JSON. {@code nodes} and {@code edges} may be JSON arrays
 * or JSON-encoded strings (the form the GUI stores).
 */
public record WorkflowCreateRequest(
    String name, JsonNode nodes, JsonNode edges, String description) {}
