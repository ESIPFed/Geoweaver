package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gw.GeoweaverApplication;
import com.gw.api.v1.service.AgentApiTokenService;
import com.gw.database.AgentApiTokenRepository;
import com.gw.jpa.AgentApiToken;
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
import org.springframework.web.server.ResponseStatusException;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiTokenServiceTest {

  @TempDir static Path tempDir;
  static Path tokenFile;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    tokenFile = tempDir.resolve(".agent_api_token");
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.api-token-file", () -> tokenFile.toString());
    registry.add("geoweaver.agent.token-ttl-days", () -> "30");
    registry.add("geoweaver.agent.token-max-ttl-days", () -> "180");
    registry.add("server.address", () -> "127.0.0.1");
  }

  @Autowired AgentApiTokenService svc;
  @Autowired AgentApiTokenRepository repository;

  @Test
  void createMatchAndKeepPrevious() {
    AgentApiTokenService.IssuedToken t1 = svc.createOrRotate();
    assertThat(t1.token()).startsWith("gwt_");
    assertThat(t1.ttlDays()).isEqualTo(30);
    assertThat(t1.expiresAt()).isAfter(Instant.now().plus(29, ChronoUnit.DAYS));
    assertThat(Files.exists(tokenFile)).isFalse();
    assertThat(svc.matches(t1.token())).isTrue();
    assertThat(svc.matches("wrong-token-value-that-is-long-enough-xxx")).isFalse();
    assertThat(t1.fingerprint()).hasSize(12);

    AgentApiTokenService.IssuedToken t2 = svc.createOrRotate();
    assertThat(t2.token()).isNotEqualTo(t1.token());
    assertThat(svc.matches(t1.token())).isTrue();
    assertThat(svc.matches(t2.token())).isTrue();
  }

  @Test
  void rotateRevokesOthers() {
    AgentApiTokenService.IssuedToken t1 = svc.createOrRotate();
    AgentApiTokenService.IssuedToken t2 = svc.issue(null, true);
    assertThat(svc.matches(t1.token())).isFalse();
    assertThat(svc.matches(t2.token())).isTrue();
  }

  @Test
  void customTtlDaysHonored() {
    AgentApiTokenService.IssuedToken issued = svc.createOrRotate(7);
    assertThat(issued.ttlDays()).isEqualTo(7);
    assertThat(issued.expiresAt())
        .isBefore(Instant.now().plus(8, ChronoUnit.DAYS))
        .isAfter(Instant.now().plus(6, ChronoUnit.DAYS));
  }

  @Test
  void ttlAboveSixMonthsRejected() {
    assertThatThrownBy(() -> svc.resolveTtlDays(181))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("180");
    assertThatThrownBy(() -> svc.resolveTtlDays(365)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void ttlZeroRejected() {
    assertThatThrownBy(() -> svc.resolveTtlDays(0)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void expiredTokenRejected() {
    AgentApiTokenService.IssuedToken issued = svc.createOrRotate(1);
    AgentApiToken row = repository.findByFingerprint(issued.fingerprint()).orElseThrow();
    row.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));
    repository.save(row);
    assertThat(svc.matches(issued.token())).isFalse();
  }

  @Test
  void revokeStopsMatch() {
    AgentApiTokenService.IssuedToken issued = svc.createOrRotate();
    assertThat(svc.revokeByFingerprint(issued.fingerprint())).isTrue();
    assertThat(svc.matches(issued.token())).isFalse();
    assertThat(svc.listMetadata()).anyMatch(m -> issued.fingerprint().equals(m.fingerprint()) && m.revokedAt() != null);
  }
}
