package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiTokenMigrationTest {

  private static final String LEGACY =
      "gwt_0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef";

  @TempDir static Path tempDir;
  static Path tokenFile;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) throws Exception {
    tokenFile = tempDir.resolve(".agent_api_token");
    String json =
        """
        {
          "v" : 1,
          "token" : "%s",
          "expiresAt" : "%s",
          "ttlDays" : 30
        }
        """
            .formatted(LEGACY, Instant.now().plus(20, ChronoUnit.DAYS));
    Files.writeString(tokenFile, json, StandardCharsets.UTF_8);
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.api-token-file", () -> tokenFile.toString());
    registry.add("server.address", () -> "127.0.0.1");
  }

  @Autowired AgentApiTokenService tokenService;

  @Test
  void leftoverFileImportedAsHashThenDeleted() {
    assertThat(tokenService.matches(LEGACY)).isTrue();
    assertThat(Files.exists(tokenFile)).isFalse();
  }
}
