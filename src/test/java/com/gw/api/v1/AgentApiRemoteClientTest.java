package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.gw.GeoweaverApplication;
import com.gw.utils.BaseTool;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Caller uses a plain HTTP client (not TestRestTemplate internals) against a JVM bound on all
 * interfaces — the same pattern as another computer talking to this Geoweaver host.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = GeoweaverApplication.class)
class AgentApiRemoteClientTest {

  private static final String KNOWN_PASSWORD = "agent-remote-client-pw-7c2d";

  @TempDir static Path tempDir;

  static Path tokenFile;

  @DynamicPropertySource
  static void agentProps(DynamicPropertyRegistry registry) {
    tokenFile = tempDir.resolve(".agent_api_token");
    registry.add("geoweaver.agent.api-enabled", () -> "true");
    registry.add("geoweaver.agent.allow-localhost-runs", () -> "true");
    registry.add("geoweaver.agent.allow-non-loopback", () -> "true");
    registry.add("geoweaver.agent.api-token-file", () -> tokenFile.toString());
    registry.add("geoweaver.agent.token-create-rate-limit-per-minute", () -> "30");
    registry.add("server.address", () -> "0.0.0.0");
  }

  @LocalServerPort int port;

  @Autowired BaseTool baseTool;

  @BeforeEach
  void setUp() throws Exception {
    baseTool.setLocalhostPassword(KNOWN_PASSWORD, true);
    GeoweaverApplication.addLocalhost();
  }

  @Test
  @DisplayName("HTTP mint + Bearer capabilities from a separate HTTP client")
  void mintAndCallAsSeparateClient() throws Exception {
    String base = "http://127.0.0.1:" + port + "/Geoweaver";
    AgentTokenRemoteMint.Result minted =
        AgentTokenRemoteMint.createToken(base, KNOWN_PASSWORD, 7, false);
    assertThat(minted.httpStatus()).isEqualTo(201);
    assertThat(minted.token()).startsWith("gwt_");
    assertThat(minted.fingerprint()).hasSize(12);

    HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    HttpResponse<String> caps =
        http.send(
            HttpRequest.newBuilder()
                .uri(URI.create(base + "/api/v1/capabilities"))
                .header("Authorization", "Bearer " + minted.token())
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(caps.statusCode()).isEqualTo(200);
    assertThat(caps.body()).contains("another computer");
    assertThat(caps.body()).doesNotContain(minted.token());
  }

  @Test
  @DisplayName("wrong password from HTTP client is 403 (not a local DB write)")
  void remoteMintWrongPassword() {
    String base = "http://127.0.0.1:" + port + "/Geoweaver";
    assertThatThrownBy(() -> AgentTokenRemoteMint.createToken(base, "wrong-password", 7, false))
        .hasMessageContaining("403");
  }

  @Test
  @DisplayName("LAN IPv4 bind accepts health from this host's non-loopback address")
  void lanAddressHealthIfPresent() throws Exception {
    String lan = firstSiteLocalIpv4();
    assumeTrue(lan != null, "no site-local IPv4 (CI without LAN) — skip two-NIC smoke");
    String url = "http://" + lan + ":" + port + "/Geoweaver/api/v1/health";
    HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
    HttpResponse<String> res =
        http.send(
            HttpRequest.newBuilder().uri(URI.create(url)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    assertThat(res.statusCode()).isEqualTo(200);
    assertThat(res.body()).contains("\"status\":\"up\"");
  }

  static String firstSiteLocalIpv4() throws Exception {
    for (NetworkInterface ni : Collections.list(NetworkInterface.getNetworkInterfaces())) {
      if (!ni.isUp() || ni.isLoopback()) {
        continue;
      }
      for (InetAddress a : Collections.list(ni.getInetAddresses())) {
        if (a instanceof Inet4Address && a.isSiteLocalAddress() && !a.isLoopbackAddress()) {
          return a.getHostAddress();
        }
      }
    }
    return null;
  }
}
