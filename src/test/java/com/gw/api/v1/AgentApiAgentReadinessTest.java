package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * What a remote coding agent needs beyond the happy path — JSON errors (never an HTML
 * redirect), natural workflow JSON, graph validation, per-step workflow status, cleanup, waiting,
 * and a machine-readable schema. Runs with {@code geoweaver.login_required=true} like a deployed
 * server.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiAgentReadinessTest {

  @TempDir static Path tempDir;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-localhost-runs", () -> "true");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.acknowledge-network-risk", () -> "true");
    registry.add(
        "geoweaver.agent.api-token-file", () -> tempDir.resolve(".agent_api_token").toString());
    registry.add("geoweaver.login_required", () -> "true");
    registry.add("server.address", () -> "127.0.0.1");
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  @Autowired AgentApiTokenService tokenService;
  @Autowired ObjectMapper mapper;

  String token;
  String base;

  @BeforeEach
  void setUp() {
    base = "http://127.0.0.1:" + port + "/Geoweaver/api/v1";
    token = tokenService.createOrRotate().token();
    GeoweaverApplication.addLocalhost();
  }

  private HttpHeaders authJson() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(token);
    return h;
  }

  private ResponseEntity<String> exchange(HttpMethod method, String path, Object body) {
    return rest.exchange(base + path, method, new HttpEntity<>(body, authJson()), String.class);
  }

  private JsonNode json(ResponseEntity<String> res) throws Exception {
    return mapper.readTree(res.getBody());
  }

  private void assertJsonError(ResponseEntity<String> res, HttpStatus status, String code)
      throws Exception {
    assertThat(res.getStatusCode()).isEqualTo(status);
    assertThat(res.getHeaders().getLocation()).isNull();
    assertThat(json(res).path("error").asText()).isEqualTo(code);
    assertThat(json(res).path("message").asText()).isNotBlank();
  }

  private String createProcess(String name, String lang, String code) throws Exception {
    ResponseEntity<String> res =
        exchange(
            HttpMethod.POST, "/processes", Map.of("name", name, "lang", lang, "code", code));
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    return json(res).get("id").asText();
  }

  @Test
  @DisplayName("malformed body, unknown path, and unmapped method return JSON, not a login redirect")
  void errorsAreJson() throws Exception {
    HttpHeaders h = authJson();
    ResponseEntity<String> badJson =
        rest.exchange(
            base + "/processes", HttpMethod.POST, new HttpEntity<>("not json", h), String.class);
    assertJsonError(badJson, HttpStatus.BAD_REQUEST, "bad_request");

    assertJsonError(exchange(HttpMethod.GET, "/nope", null), HttpStatus.NOT_FOUND, "not_found");
    assertJsonError(
        exchange(HttpMethod.PUT, "/runs/abc", Map.of()), HttpStatus.NOT_FOUND, "not_found");
    assertJsonError(
        exchange(HttpMethod.GET, "/runs/does-not-exist?waitSeconds=abc", null),
        HttpStatus.BAD_REQUEST,
        "bad_request");
  }

  @Test
  @DisplayName("real endpoints still win over the catch-all mapping")
  void routingNotShadowed() throws Exception {
    assertThat(exchange(HttpMethod.GET, "/capabilities", null).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertThat(exchange(HttpMethod.GET, "/processes", null).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertThat(exchange(HttpMethod.GET, "/workflows", null).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertThat(exchange(HttpMethod.GET, "/tokens", null).getStatusCode()).isEqualTo(HttpStatus.OK);
    assertJsonError(
        exchange(HttpMethod.GET, "/processes/zzzzzz", null), HttpStatus.NOT_FOUND, "not_found");
    ResponseEntity<String> anon =
        rest.getForEntity(base + "/nope", String.class);
    assertJsonError(anon, HttpStatus.UNAUTHORIZED, "unauthorized");
  }

  @Test
  @DisplayName("workflow accepts nodes/edges as arrays with shorthand edges and fills GUI fields")
  void workflowArraysAndShorthandEdges() throws Exception {
    String a = createProcess("ready-a", "shell", "echo step-a");
    String b = createProcess("ready-b", "python", "print('step-b')");
    Map<String, Object> body =
        Map.of(
            "name",
            "ready-arrays",
            "nodes",
            List.of(Map.of("id", a + "-s1"), Map.of("id", b + "-s2")),
            "edges",
            List.of(Map.of("source", a + "-s1", "target", b + "-s2")));
    ResponseEntity<String> created = exchange(HttpMethod.POST, "/workflows", body);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode wf = json(created);
    JsonNode nodes = mapper.readTree(wf.get("nodes").asText());
    assertThat(nodes.get(0).get("title").asText()).isEqualTo("ready-a");
    assertThat(nodes.get(1).get("x").isNumber()).isTrue();
    JsonNode edges = mapper.readTree(wf.get("edges").asText());
    assertThat(edges.get(0).get("source").get("id").asText()).isEqualTo(a + "-s1");
    assertThat(edges.get(0).get("target").get("title").asText()).isEqualTo("ready-b");

    String wid = wf.get("id").asText();
    ResponseEntity<String> run = exchange(HttpMethod.POST, "/workflows/" + wid + "/runs", null);
    String hid = json(run).get("historyId").asText();

    JsonNode done =
        json(
            exchange(
                HttpMethod.GET, "/runs/" + hid + "?waitSeconds=60&includeStepLogs=true", null));
    assertThat(done.get("terminal").asBoolean()).isTrue();
    assertThat(done.get("status").asText()).isEqualTo("Done");
    assertThat(done.get("workflowRun").asBoolean()).isTrue();
    JsonNode steps = done.get("steps");
    assertThat(steps.size()).isEqualTo(2);
    assertThat(steps.get(0).get("nodeId").asText()).isEqualTo(a + "-s1");
    assertThat(steps.get(0).get("processId").asText()).isEqualTo(a);
    assertThat(steps.get(0).get("status").asText()).isEqualTo("Done");
    assertThat(steps.get(0).get("log").asText()).contains("step-a");
    assertThat(steps.get(1).get("log").asText()).contains("step-b");

    JsonNode runs = json(exchange(HttpMethod.GET, "/workflows/" + wid + "/runs", null));
    assertThat(runs.isArray()).isTrue();
    assertThat(runs.get(0).get("historyId").asText()).isEqualTo(hid);
    assertThat(runs.get(0).get("steps").size()).isEqualTo(2);
  }

  @Test
  @DisplayName("failing step is identifiable from steps without extra lookups")
  void failingStepVisible() throws Exception {
    String bad = createProcess("ready-fail", "python", "raise SystemExit('boom-step')");
    Map<String, Object> body =
        Map.of("name", "ready-fail-wf", "nodes", List.of(Map.of("id", bad + "-f1")));
    String wid = json(exchange(HttpMethod.POST, "/workflows", body)).get("id").asText();
    String hid =
        json(exchange(HttpMethod.POST, "/workflows/" + wid + "/runs", null))
            .get("historyId")
            .asText();
    JsonNode done =
        json(
            exchange(
                HttpMethod.GET, "/runs/" + hid + "?waitSeconds=60&includeStepLogs=true", null));
    assertThat(done.get("status").asText()).isEqualTo("Failed");
    JsonNode step = done.get("steps").get(0);
    assertThat(step.get("status").asText()).isEqualTo("Failed");
    assertThat(step.get("log").asText()).contains("boom-step");
  }

  @Test
  @DisplayName("invalid graphs are rejected with a precise 400")
  void graphValidation() throws Exception {
    String a = createProcess("ready-v", "shell", "echo v");
    assertJsonError(
        exchange(HttpMethod.POST, "/workflows", Map.of("name", "x", "nodes", "[{oops")),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    ResponseEntity<String> missing =
        exchange(
            HttpMethod.POST,
            "/workflows",
            Map.of("name", "x", "nodes", List.of(Map.of("id", "zzzzzz-m1"))));
    assertJsonError(missing, HttpStatus.BAD_REQUEST, "bad_request");
    assertThat(json(missing).get("message").asText()).contains("zzzzzz");
    assertJsonError(
        exchange(
            HttpMethod.POST, "/workflows", Map.of("name", "x", "nodes", List.of(Map.of("id", a)))),
        HttpStatus.BAD_REQUEST,
        "bad_request");
    assertJsonError(
        exchange(
            HttpMethod.POST,
            "/workflows",
            Map.of(
                "name",
                "x",
                "nodes",
                List.of(Map.of("id", a + "-1")),
                "edges",
                List.of(Map.of("source", a + "-1", "target", a + "-9")))),
        HttpStatus.BAD_REQUEST,
        "bad_request");
  }

  @Test
  @DisplayName("delete refuses processes in use, then cleans up workflow and process")
  void deleteLifecycle() throws Exception {
    String a = createProcess("ready-del", "shell", "echo del");
    String wid =
        json(
                exchange(
                    HttpMethod.POST,
                    "/workflows",
                    Map.of("name", "ready-del-wf", "nodes", List.of(Map.of("id", a + "-d1")))))
            .get("id")
            .asText();

    ResponseEntity<String> conflict = exchange(HttpMethod.DELETE, "/processes/" + a, null);
    assertJsonError(conflict, HttpStatus.CONFLICT, "conflict");
    assertThat(json(conflict).get("message").asText()).contains(wid);

    assertThat(exchange(HttpMethod.DELETE, "/workflows/" + wid, null).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertJsonError(
        exchange(HttpMethod.GET, "/workflows/" + wid, null), HttpStatus.NOT_FOUND, "not_found");
    assertThat(exchange(HttpMethod.DELETE, "/processes/" + a, null).getStatusCode())
        .isEqualTo(HttpStatus.OK);
    assertJsonError(
        exchange(HttpMethod.GET, "/processes/" + a, null), HttpStatus.NOT_FOUND, "not_found");
  }

  @Test
  @DisplayName("agent OpenAPI schema is public and covers only /api/v1")
  void openApiSchema() throws Exception {
    ResponseEntity<String> res =
        rest.getForEntity(
            "http://127.0.0.1:" + port + "/Geoweaver/v3/api-docs/agent-api", String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode paths = json(res).get("paths");
    assertThat(paths.has("/api/v1/workflows/{id}/runs")).isTrue();
    paths.fieldNames().forEachRemaining(p -> assertThat(p).startsWith("/api/v1/"));
  }
}
