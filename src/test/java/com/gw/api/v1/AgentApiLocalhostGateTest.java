package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import com.gw.utils.BaseTool;
import java.nio.file.Path;
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
 * Password / security gate: when allow-localhost-runs=false, GUI localhost password is still
 * required even with a valid Bearer token.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiLocalhostGateTest {

  private static final String KNOWN_PASSWORD = "agent-gate-test-password-9f3a";

  @TempDir static Path tempDir;
  static Path tokenFile;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    tokenFile = tempDir.resolve(".agent_api_token");
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-localhost-runs", () -> "false");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.api-token-file", () -> tokenFile.toString());
    registry.add("server.address", () -> "127.0.0.1");
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  @Autowired AgentApiTokenService tokenService;
  @Autowired ObjectMapper mapper;
  @Autowired BaseTool baseTool;

  String token;
  String base;

  @BeforeEach
  void setUp() throws Exception {
    base = "http://127.0.0.1:" + port + "/Geoweaver/api/v1";
    token = tokenService.createOrRotate().token();
    GeoweaverApplication.addLocalhost();
    baseTool.setLocalhostPassword(KNOWN_PASSWORD, true);
  }

  private HttpHeaders authJson() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(token);
    return h;
  }

  private String createShellProcess() throws Exception {
    ResponseEntity<String> created =
        rest.exchange(
            base + "/processes",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("name", "gate-proc", "lang", "shell", "code", "echo gate-ok"),
                authJson()),
            String.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
    return mapper.readTree(created.getBody()).get("id").asText();
  }

  @Test
  @DisplayName("token alone cannot run localhost when allow-localhost-runs=false")
  void forbidWithoutPassword() throws Exception {
    String id = createShellProcess();
    ResponseEntity<String> run =
        rest.exchange(
            base + "/processes/" + id + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("hostId", "100001"), authJson()),
            String.class);
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(mapper.readTree(run.getBody()).get("error").asText()).isEqualTo("forbidden");
  }

  @Test
  @DisplayName("wrong localhost password is rejected (security still enforced)")
  void wrongPasswordForbidden() throws Exception {
    String id = createShellProcess();
    ResponseEntity<String> run =
        rest.exchange(
            base + "/processes/" + id + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("hostId", "100001", "hostPassword", "definitely-wrong-password"),
                authJson()),
            String.class);
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    JsonNode err = mapper.readTree(run.getBody());
    assertThat(err.get("error").asText()).isEqualTo("forbidden");
  }

  @Test
  @DisplayName("correct localhost password allows run when flag is false")
  void correctPasswordAllowsRun() throws Exception {
    String id = createShellProcess();
    ResponseEntity<String> run =
        rest.exchange(
            base + "/processes/" + id + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of(
                    "hostId",
                    "100001",
                    "hostPassword",
                    KNOWN_PASSWORD,
                    "clientRunKey",
                    "gate-ok-1"),
                authJson()),
            String.class);
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(mapper.readTree(run.getBody()).get("historyId").asText()).isNotBlank();
  }

  @Test
  @DisplayName("workflow run also requires password when flag is false")
  void workflowRequiresPassword() throws Exception {
    String processId = createShellProcess();
    String nodeId = processId + "-n0001";
    String nodes =
        "[{\"title\":\"gate\",\"id\":\"" + nodeId + "\",\"x\":10,\"y\":10}]";
    ResponseEntity<String> wf =
        rest.exchange(
            base + "/workflows",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("name", "gate-wf", "nodes", nodes, "edges", "[]"), authJson()),
            String.class);
    String wid = mapper.readTree(wf.getBody()).get("id").asText();

    ResponseEntity<String> denied =
        rest.exchange(
            base + "/workflows/" + wid + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("mode", "one"), authJson()),
            String.class);
    assertThat(denied.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);

    ResponseEntity<String> allowed =
        rest.exchange(
            base + "/workflows/" + wid + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("mode", "one", "hostPassword", KNOWN_PASSWORD, "clientRunKey", "wf-gate-1"),
                authJson()),
            String.class);
    assertThat(allowed.getStatusCode()).isEqualTo(HttpStatus.OK);
  }
}
