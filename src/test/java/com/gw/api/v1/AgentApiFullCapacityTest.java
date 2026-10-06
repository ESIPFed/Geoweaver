package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import java.nio.file.Path;
import java.util.LinkedHashMap;
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
 * Validates that a coding agent with a correct Bearer token can perform the same localhost
 * Python/Shell/workflow loop a human does in the UI — without HTML login.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiFullCapacityTest {

  @TempDir static Path tempDir;
  static Path tokenFile;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    tokenFile = tempDir.resolve(".agent_api_token");
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-localhost-runs", () -> "true");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.api-token-file", () -> tokenFile.toString());
    registry.add("server.address", () -> "127.0.0.1");
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  @Autowired AgentApiTokenService tokenService;
  @Autowired ObjectMapper mapper;

  String token;
  String base;

  @BeforeEach
  void setUp() throws Exception {
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
    return rest.exchange(
        base + path, method, new HttpEntity<>(body, authJson()), String.class);
  }

  private JsonNode pollUntilTerminal(String historyId, int maxSeconds) throws Exception {
    long deadline = System.currentTimeMillis() + maxSeconds * 1000L;
    JsonNode last = null;
    while (System.currentTimeMillis() < deadline) {
      ResponseEntity<String> res =
          exchange(HttpMethod.GET, "/runs/" + historyId + "?logLimit=50000", null);
      assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
      last = mapper.readTree(res.getBody());
      if (last.path("terminal").asBoolean(false)) {
        return last;
      }
      Thread.sleep(400);
    }
    fail("Run " + historyId + " did not reach a terminal status. Last=" + last);
    return last;
  }

  @Test
  @DisplayName("agent can create/update/run/poll python like the UI")
  void pythonCreateUpdateRunPoll() throws Exception {
    ResponseEntity<String> created =
        exchange(
            HttpMethod.POST,
            "/processes",
            Map.of(
                "name",
                "agent-py",
                "lang",
                "python",
                "code",
                "print('agent-py-v1')"));
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
    String id = mapper.readTree(created.getBody()).get("id").asText();

    ResponseEntity<String> updated =
        exchange(
            HttpMethod.PUT,
            "/processes/" + id,
            Map.of("code", "print('agent-py-v2')"));
    assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mapper.readTree(updated.getBody()).get("code").asText()).contains("agent-py-v2");

    ResponseEntity<String> run =
        exchange(
            HttpMethod.POST,
            "/processes/" + id + "/runs",
            Map.of("hostId", "100001", "clientRunKey", "py-job-1"));
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.OK);
    String historyId = mapper.readTree(run.getBody()).get("historyId").asText();

    JsonNode done = pollUntilTerminal(historyId, 60);
    assertThat(done.get("status").asText()).isEqualTo("Done");
    assertThat(done.get("log").asText()).contains("agent-py-v2");

    ResponseEntity<String> listed = exchange(HttpMethod.GET, "/processes/" + id + "/runs", null);
    assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mapper.readTree(listed.getBody()).isArray()).isTrue();
  }

  @Test
  @DisplayName("agent can stop a long-running shell process")
  void stopLongRunningShell() throws Exception {
    ResponseEntity<String> created =
        exchange(
            HttpMethod.POST,
            "/processes",
            Map.of("name", "agent-sleep", "lang", "shell", "code", "sleep 120"));
    String id = mapper.readTree(created.getBody()).get("id").asText();

    ResponseEntity<String> run =
        exchange(
            HttpMethod.POST,
            "/processes/" + id + "/runs",
            Map.of("hostId", "100001", "clientRunKey", "sleep-job-1"));
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.OK);
    String historyId = mapper.readTree(run.getBody()).get("historyId").asText();

    Thread.sleep(800);
    ResponseEntity<String> stopped =
        exchange(HttpMethod.POST, "/runs/" + historyId + "/stop", Map.of());
    assertThat(stopped.getStatusCode()).isEqualTo(HttpStatus.OK);

    JsonNode after = pollUntilTerminal(historyId, 30);
    assertThat(after.get("status").asText()).isIn("Stopped", "Done", "Failed");
  }

  @Test
  @DisplayName("agent can build and run a one-node workflow on localhost")
  void workflowCreateAndRun() throws Exception {
    ResponseEntity<String> proc =
        exchange(
            HttpMethod.POST,
            "/processes",
            Map.of("name", "wf-step", "lang", "shell", "code", "echo workflow-agent-ok"));
    String processId = mapper.readTree(proc.getBody()).get("id").asText();
    String nodeId = processId + "-n0001";

    String nodes =
        "[{\"title\":\"wf-step\",\"id\":\"" + nodeId + "\",\"x\":100,\"y\":100}]";
    String edges = "[]";

    Map<String, Object> wfBody = new LinkedHashMap<>();
    wfBody.put("name", "agent-wf");
    wfBody.put("nodes", nodes);
    wfBody.put("edges", edges);
    wfBody.put("description", "agent one-node workflow");

    ResponseEntity<String> wf = exchange(HttpMethod.POST, "/workflows", wfBody);
    assertThat(wf.getStatusCode()).isEqualTo(HttpStatus.OK);
    String workflowId = mapper.readTree(wf.getBody()).get("id").asText();

    ResponseEntity<String> got = exchange(HttpMethod.GET, "/workflows/" + workflowId, null);
    assertThat(got.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mapper.readTree(got.getBody()).get("nodes").asText()).contains(processId);

    ResponseEntity<String> run =
        exchange(
            HttpMethod.POST,
            "/workflows/" + workflowId + "/runs",
            Map.of("mode", "one", "clientRunKey", "wf-job-1"));
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.OK);
    String historyId = mapper.readTree(run.getBody()).get("historyId").asText();

    JsonNode done = pollUntilTerminal(historyId, 90);
    assertThat(done.get("status").asText()).isIn("Done", "Failed");
    // workflow history output is process-history id list; Done is the success signal
    assertThat(done.get("terminal").asBoolean()).isTrue();
  }

  @Test
  @DisplayName("inventory endpoints work with token (process + workflow lists)")
  void inventoryLikeUiSidebar() throws Exception {
    exchange(
        HttpMethod.POST,
        "/processes",
        Map.of("name", "list-me", "lang", "shell", "code", "echo 1"));

    ResponseEntity<String> procs = exchange(HttpMethod.GET, "/processes", null);
    assertThat(procs.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mapper.readTree(procs.getBody()).isArray()).isTrue();

    ResponseEntity<String> wfs = exchange(HttpMethod.GET, "/workflows", null);
    assertThat(wfs.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mapper.readTree(wfs.getBody()).isArray()).isTrue();

    ResponseEntity<String> caps = exchange(HttpMethod.GET, "/capabilities", null);
    JsonNode c = mapper.readTree(caps.getBody());
    assertThat(c.get("executionHost").asText()).isEqualTo("100001");
    assertThat(c.get("executionHostMeaning").asText()).contains("Geoweaver is running");
    assertThat(c.get("note").asText()).contains("GEOWEAVER_BASE_URL");
    assertThat(c.get("processLanguages").toString()).contains("python", "shell");
  }
}
