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

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiTokenBootstrapTest {

  private static final String KNOWN_PASSWORD = "agent-bootstrap-pw-9f3a";

  @TempDir static Path tempDir;

  static Path tokenFile;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    tokenFile = tempDir.resolve(".agent_api_token");
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-localhost-runs", () -> "false");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.api-token-file", () -> tokenFile.toString());
    registry.add("geoweaver.agent.token-create-rate-limit-per-minute", () -> "30");
    registry.add("server.address", () -> "127.0.0.1");
  }

  @LocalServerPort int port;

  @Autowired TestRestTemplate rest;

  @Autowired AgentApiTokenService tokenService;

  @Autowired BaseTool baseTool;

  @Autowired ObjectMapper mapper;

  String base;

  @BeforeEach
  void setUp() throws Exception {
    base = "http://127.0.0.1:" + port + "/Geoweaver/api/v1";
    baseTool.setLocalhostPassword(KNOWN_PASSWORD, true);
    GeoweaverApplication.addLocalhost();
  }

  @Test
  @DisplayName("remote token create with correct localhost password (no Bearer)")
  void createTokenWithPassword() throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> res =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("hostPassword", KNOWN_PASSWORD), h),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode n = mapper.readTree(res.getBody());
    String newToken = n.get("token").asText();
    assertThat(newToken).startsWith("gwt_");
    assertThat(n.get("fingerprint").asText()).isNotBlank();
    assertThat(n.get("expiresAt").asText()).isNotBlank();
    assertThat(n.get("ttlDays").asInt()).isBetween(1, 180);
    assertThat(tokenService.matches(newToken)).isTrue();
    assertThat(java.nio.file.Files.exists(tokenFile)).isFalse();

    // New token can call capabilities
    HttpHeaders auth = new HttpHeaders();
    auth.setBearerAuth(newToken);
    ResponseEntity<String> caps =
        rest.exchange(base + "/capabilities", HttpMethod.GET, new HttpEntity<>(auth), String.class);
    assertThat(caps.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  @DisplayName("wrong localhost password cannot mint token")
  void wrongPasswordRejected() throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> res =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("hostPassword", "not-the-password"), h),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(mapper.readTree(res.getBody()).get("error").asText()).isEqualTo("forbidden");
  }

  @Test
  @DisplayName("missing hostPassword is bad request")
  void missingPassword() throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> res =
        rest.exchange(
            base + "/tokens", HttpMethod.POST, new HttpEntity<>("{}", h), String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("token create keeps previous Bearer unless revokeOthers")
  void createKeepsOldUnlessRotate() throws Exception {
    String old = tokenService.createOrRotate().token();
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> res =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("hostPassword", KNOWN_PASSWORD), h),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode n = mapper.readTree(res.getBody());
    assertThat(n.get("previousTokenInvalidated").asBoolean()).isFalse();
    String neu = n.get("token").asText();
    assertThat(tokenService.matches(old)).isTrue();
    assertThat(tokenService.matches(neu)).isTrue();

    ResponseEntity<String> rotated =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("hostPassword", KNOWN_PASSWORD, "revokeOthers", true), h),
            String.class);
    assertThat(rotated.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode n2 = mapper.readTree(rotated.getBody());
    assertThat(n2.get("previousTokenInvalidated").asBoolean()).isTrue();
    String newest = n2.get("token").asText();
    assertThat(tokenService.matches(old)).isFalse();
    assertThat(tokenService.matches(neu)).isFalse();
    assertThat(tokenService.matches(newest)).isTrue();
  }

  @Test
  @DisplayName("ttlDays above six months rejected")
  void ttlTooLongRejected() throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> res =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("hostPassword", KNOWN_PASSWORD, "ttlDays", 181), h),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  @DisplayName("custom ttlDays accepted within six months")
  void customTtlAccepted() throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> res =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(
                Map.of("hostPassword", KNOWN_PASSWORD, "ttlDays", 14), h),
            String.class);
    assertThat(res.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    JsonNode n = mapper.readTree(res.getBody());
    assertThat(n.get("ttlDays").asInt()).isEqualTo(14);
    assertThat(n.get("expiresAt").asText()).isNotBlank();
  }

  @Test
  @DisplayName("list and revoke tokens by fingerprint")
  void listAndRevoke() throws Exception {
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    ResponseEntity<String> created =
        rest.exchange(
            base + "/tokens",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("hostPassword", KNOWN_PASSWORD), h),
            String.class);
    JsonNode n = mapper.readTree(created.getBody());
    String tok = n.get("token").asText();
    String fp = n.get("fingerprint").asText();

    HttpHeaders auth = new HttpHeaders();
    auth.setBearerAuth(tok);
    ResponseEntity<String> listed =
        rest.exchange(base + "/tokens", HttpMethod.GET, new HttpEntity<>(auth), String.class);
    assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(listed.getBody()).contains(fp);
    assertThat(listed.getBody()).doesNotContain(tok);

    ResponseEntity<String> revoked =
        rest.exchange(
            base + "/tokens/" + fp, HttpMethod.DELETE, new HttpEntity<>(auth), String.class);
    assertThat(revoked.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(tokenService.matches(tok)).isFalse();
  }
}
