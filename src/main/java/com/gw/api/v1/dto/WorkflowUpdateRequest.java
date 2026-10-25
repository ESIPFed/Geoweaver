package com.gw.api.v1.dto;

import com.fasterxml.jackson.databind.JsonNode;

/** Partial workflow update. {@code nodes}/{@code edges} accept JSON arrays or encoded strings. */
public record WorkflowUpdateRequest(
    String name, JsonNode nodes, JsonNode edges, String description) {}
