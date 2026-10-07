package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiIntegrationTest {

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

  private HttpHeaders authHeaders() {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(token);
    return h;
  }

  @Test
  @DisplayName("public health has no secrets")
  void healthPublic() throws Exception {
    ResponseEntity<String> res = rest.getForEntity(base + "/health", String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode n = mapper.readTree(res.getBody());
    assertThat(n.get("status").asText()).isEqualTo("up");
    assertThat(n.fieldNames()).toIterable().containsExactly("status");
  }

  @Test
  @DisplayName("capabilities without token returns 401 JSON")
  void capabilitiesUnauthorized() throws Exception {
    ResponseEntity<String> res = rest.getForEntity(base + "/capabilities", String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    JsonNode n = mapper.readTree(res.getBody());
    assertThat(n.get("error").asText()).isEqualTo("unauthorized");
    assertThat(res.getHeaders().getContentType().toString()).contains("json");
  }

  @Test
  @DisplayName("create shell process, idempotent run, get history")
  void processCrudAndIdempotentRun() throws Exception {
    Map<String, Object> body =
        Map.of(
            "name",
            "agent-api-test-proc",
            "lang",
            "shell",
            "code",
            "echo agent-api-ok",
            "description",
            "shell");
    ResponseEntity<String> created =
        rest.exchange(
            base + "/processes",
            HttpMethod.POST,
            new HttpEntity<>(body, authHeaders()),
            String.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode proc = mapper.readTree(created.getBody());
    String processId = proc.get("id").asText();

    Map<String, Object> runBody = Map.of("hostId", "100001", "clientRunKey", "idem-key-1");
    ResponseEntity<String> run1 =
        rest.exchange(
            base + "/processes/" + processId + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(runBody, authHeaders()),
            String.class);
    assertThat(run1.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode r1 = mapper.readTree(run1.getBody());
    String historyId = r1.get("historyId").asText();
    assertThat(r1.get("reusedExisting").asBoolean()).isFalse();

    ResponseEntity<String> run2 =
        rest.exchange(
            base + "/processes/" + processId + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(runBody, authHeaders()),
            String.class);
    JsonNode r2 = mapper.readTree(run2.getBody());
    assertThat(r2.get("historyId").asText()).isEqualTo(historyId);
    assertThat(r2.get("reusedExisting").asBoolean()).isTrue();

    ResponseEntity<String> getRun =
        rest.exchange(
            base + "/runs/" + historyId + "?logLimit=4000",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class);
    assertThat(getRun.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode hist = mapper.readTree(getRun.getBody());
    assertThat(hist.get("historyId").asText()).isEqualTo(historyId);
    assertThat(hist.get("status").asText())
        .isIn("Done", "Failed", "Running", "Unknown", "Stopped", "Ready", "Skipped");
  }

  @Test
  @DisplayName("HIGH-008: remote hostId rejected")
  void rejectRemoteHost() throws Exception {
    ResponseEntity<String> created =
        rest.exchange(
            base + "/processes",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("name", "remote-gate", "lang", "shell", "code", "echo x"), authHeaders()),
            String.class);
    String id = mapper.readTree(created.getBody()).get("id").asText();

    ResponseEntity<String> run =
        rest.exchange(
            base + "/processes/" + id + "/runs",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("hostId", "ssh-remote-99", "hostPassword", "secret"), authHeaders()),
            String.class);
    assertThat(run.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    assertThat(mapper.readTree(run.getBody()).get("error").asText()).isEqualTo("bad_request");
  }

  @Test
  @DisplayName("HIGH-009: missing history returns 404")
  void missingHistoryNotFound() throws Exception {
    ResponseEntity<String> getRun =
        rest.exchange(
            base + "/runs/does-not-exist-zzzz",
            HttpMethod.GET,
            new HttpEntity<>(authHeaders()),
            String.class);
    assertThat(getRun.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    assertThat(mapper.readTree(getRun.getBody()).get("error").asText()).isEqualTo("not_found");
  }

  @Test
  @DisplayName("reject non python/shell lang")
  void rejectJupyterLang() throws Exception {
    ResponseEntity<String> created =
        rest.exchange(
            base + "/processes",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("name", "nb", "lang", "jupyter", "code", "{}"), authHeaders()),
            String.class);
    assertThat(created.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("old token rejected after revoke; extra issued tokens stay valid")
  void revokeInvalidatesOneToken() throws Exception {
    String old = token;
    AgentApiTokenService.IssuedToken neu = tokenService.createOrRotate();
    ResponseEntity<String> withOldStill =
        rest.exchange(
            base + "/capabilities",
            HttpMethod.GET,
            new HttpEntity<>(bearer(old)),
            String.class);
    assertThat(withOldStill.getStatusCode()).isEqualTo(HttpStatus.OK);

    assertThat(tokenService.revokeByFingerprint(tokenService.fingerprint(old))).isTrue();
    ResponseEntity<String> withOld =
        rest.exchange(
            base + "/capabilities",
            HttpMethod.GET,
            new HttpEntity<>(bearer(old)),
            String.class);
    assertThat(withOld.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    ResponseEntity<String> withNew =
        rest.exchange(
            base + "/capabilities",
            HttpMethod.GET,
            new HttpEntity<>(bearer(neu.token())),
            String.class);
    assertThat(withNew.getStatusCode()).isEqualTo(HttpStatus.OK);
    JsonNode caps = mapper.readTree(withNew.getBody());
    assertThat(caps.get("processLanguages").toString()).contains("python").contains("shell");
    assertThat(caps.get("operations").toString()).contains("startWorkflowRun");
    assertThat(caps.get("tokenStoredInDatabase").asBoolean()).isTrue();
  }

  private static HttpHeaders bearer(String t) {
    HttpHeaders h = new HttpHeaders();
    h.setBearerAuth(t);
    return h;
  }
}
