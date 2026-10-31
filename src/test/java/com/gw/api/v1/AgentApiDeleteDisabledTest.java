package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import java.nio.file.Path;
import java.util.Map;
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

/** {@code geoweaver.agent.allow-delete=false} blocks agent deletes but not create. */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiDeleteDisabledTest {

  @TempDir static Path tempDir;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-delete", () -> "false");
    registry.add("geoweaver.agent.acknowledge-network-risk", () -> "true");
    registry.add(
        "geoweaver.agent.api-token-file", () -> tempDir.resolve(".agent_api_token").toString());
    registry.add("server.address", () -> "127.0.0.1");
  }

  @LocalServerPort int port;
  @Autowired TestRestTemplate rest;
  @Autowired AgentApiTokenService tokenService;
  @Autowired ObjectMapper mapper;

  @Test
  void deleteForbiddenWhenDisabled() throws Exception {
    String base = "http://127.0.0.1:" + port + "/Geoweaver/api/v1";
    HttpHeaders h = new HttpHeaders();
    h.setContentType(MediaType.APPLICATION_JSON);
    h.setBearerAuth(tokenService.createOrRotate().token());
    ResponseEntity<String> created =
        rest.exchange(
            base + "/processes",
            HttpMethod.POST,
            new HttpEntity<>(Map.of("name", "keep", "lang", "shell", "code", "echo"), h),
            String.class);
    String id = mapper.readTree(created.getBody()).get("id").asText();

    ResponseEntity<String> denied =
        rest.exchange(
            base + "/processes/" + id, HttpMethod.DELETE, new HttpEntity<>(h), String.class);
    assertThat(denied.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    assertThat(mapper.readTree(denied.getBody()).get("error").asText()).isEqualTo("forbidden");
    assertThat(
            rest.exchange(
                    base + "/processes/" + id, HttpMethod.GET, new HttpEntity<>(h), String.class)
                .getStatusCode())
        .isEqualTo(HttpStatus.OK);
  }
}
