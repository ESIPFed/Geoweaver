package com.gw.api.v1.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.gw.database.ProcessRepository;
import com.gw.jpa.GWProcess;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Validates and normalizes Agent API workflow graphs into the JSON strings Geoweaver stores.
 *
 * <p>Accepts {@code nodes}/{@code edges} as JSON arrays or JSON-encoded strings. Node ids must be
 * {@code {processId}-{suffix}} and reference an existing process. Edges may use {@code
 * "source":"nodeId"} shorthand or the GUI form {@code "source":{"id":"nodeId",...}}; both are
 * expanded to the GUI form so the canvas renders them. Missing {@code title}/{@code x}/{@code y}
 * are filled in.
 */
@Component
public class WorkflowGraphValidator {

  private static final ObjectMapper MAPPER = new ObjectMapper();
  static final String NODE_FORMAT_HINT =
      "Each node needs id \"{processId}-{suffix}\" (e.g. \"ab12cd-step1\") where processId is an"
          + " existing process from GET /api/v1/processes";

  private final ProcessRepository processRepository;

  public WorkflowGraphValidator(ProcessRepository processRepository) {
    this.processRepository = processRepository;
  }

  /** Normalized graph as stored strings. */
  public record Graph(String nodes, String edges) {}

  public Graph normalize(JsonNode nodesInput, JsonNode edgesInput) {
    ArrayNode nodes = asArray(nodesInput, "nodes", false);
    ArrayNode edges = asArray(edgesInput, "edges", true);
    if (nodes.isEmpty()) {
      throw badRequest("nodes must contain at least one node. " + NODE_FORMAT_HINT);
    }

    Map<String, ObjectNode> byId = new LinkedHashMap<>();
    for (int i = 0; i < nodes.size(); i++) {
      JsonNode n = nodes.get(i);
      if (!n.isObject()) {
        throw badRequest("nodes[" + i + "] must be an object. " + NODE_FORMAT_HINT);
      }
      ObjectNode node = (ObjectNode) n;
      String id = node.path("id").asText("").trim();
      int dash = id.indexOf('-');
      if (dash <= 0 || dash == id.length() - 1) {
        throw badRequest("nodes[" + i + "].id \"" + id + "\" is invalid. " + NODE_FORMAT_HINT);
      }
      if (byId.containsKey(id)) {
        throw badRequest("nodes[" + i + "].id \"" + id + "\" is duplicated; suffixes must be unique");
      }
      String processId = id.substring(0, dash);
      Optional<GWProcess> process = processRepository.findById(processId);
      if (process.isEmpty()) {
        throw badRequest(
            "nodes[" + i + "] references process \"" + processId + "\" which does not exist");
      }
      node.put("id", id);
      AgentSearch.requireNoMarkup(node.path("title").asText(null), "nodes[" + i + "].title");
      if (node.path("title").asText("").isBlank()) {
        node.put("title", process.get().getName());
      }
      if (!node.path("x").isNumber()) {
        node.put("x", 100 + 200 * i);
      }
      if (!node.path("y").isNumber()) {
        node.put("y", 200);
      }
      byId.put(id, node);
    }

    ArrayNode outEdges = MAPPER.createArrayNode();
    for (int i = 0; i < edges.size(); i++) {
      JsonNode e = edges.get(i);
      if (!e.isObject()) {
        throw badRequest("edges[" + i + "] must be an object with source and target");
      }
      String source = endpointId(e.get("source"));
      String target = endpointId(e.get("target"));
      for (String[] end : new String[][] {{"source", source}, {"target", target}}) {
        if (end[1] == null || !byId.containsKey(end[1])) {
          throw badRequest(
              "edges[" + i + "]." + end[0] + " \"" + end[1] + "\" is not a node id in nodes");
        }
      }
      if (source.equals(target)) {
        throw badRequest("edges[" + i + "] links node \"" + source + "\" to itself");
      }
      ObjectNode edge = MAPPER.createObjectNode();
      edge.set("source", byId.get(source).deepCopy());
      edge.set("target", byId.get(target).deepCopy());
      outEdges.add(edge);
    }

    try {
      return new Graph(MAPPER.writeValueAsString(nodes), MAPPER.writeValueAsString(outEdges));
    } catch (JsonProcessingException ex) {
      throw badRequest("Could not serialize workflow graph");
    }
  }

  private static String endpointId(JsonNode end) {
    if (end == null || end.isNull()) {
      return null;
    }
    if (end.isTextual()) {
      return end.asText().trim();
    }
    if (end.isObject() && end.hasNonNull("id")) {
      return end.get("id").asText().trim();
    }
    return null;
  }

  private static ArrayNode asArray(JsonNode input, String field, boolean emptyIfMissing) {
    if (input == null || input.isNull() || (input.isTextual() && input.asText().isBlank())) {
      if (emptyIfMissing) {
        return MAPPER.createArrayNode();
      }
      throw badRequest(field + " is required. " + NODE_FORMAT_HINT);
    }
    JsonNode parsed = input;
    if (input.isTextual()) {
      try {
        parsed = MAPPER.readTree(input.asText());
      } catch (JsonProcessingException ex) {
        throw badRequest(field + " is not valid JSON: " + ex.getOriginalMessage());
      }
    }
    if (parsed == null || !parsed.isArray()) {
      throw badRequest(field + " must be a JSON array (or a string containing one)");
    }
    return (ArrayNode) parsed.deepCopy();
  }

  private static ResponseStatusException badRequest(String msg) {
    return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
  }
}
