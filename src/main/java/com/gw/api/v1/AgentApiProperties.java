package com.gw.api.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Configuration for the Geoweaver Agent HTTP API (`/api/v1`). */
@Component
public class AgentApiProperties {

  @Value("${geoweaver.agent.api-enabled:false}")
  private boolean apiEnabled;

  @Value("${geoweaver.agent.allow-localhost-runs:false}")
  private boolean allowLocalhostRuns;

  @Value("${geoweaver.agent.allow-non-loopback:false}")
  private boolean allowNonLoopback;

  @Value("${geoweaver.agent.api-token-file:${user.home}/gw-workspace/.agent_api_token}")
  private String apiTokenFile;

  @Value("${geoweaver.agent.run-rate-limit-per-minute:30}")
  private int runRateLimitPerMinute;

  /** Password-gated remote token create attempts per client IP per minute. */
  @Value("${geoweaver.agent.token-create-rate-limit-per-minute:5}")
  private int tokenCreateRateLimitPerMinute;

  /** Default Bearer token lifetime in days (capped by token-max-ttl-days and 180). */
  @Value("${geoweaver.agent.token-ttl-days:30}")
  private int tokenTtlDays;

  /** Maximum allowed Bearer token lifetime in days (never above 180 / six months). */
  @Value("${geoweaver.agent.token-max-ttl-days:180}")
  private int tokenMaxTtlDays;

  @Value("${server.address:}")
  private String serverAddress;

  @Value("${server.port:8070}")
  private int serverPort;

  public boolean isApiEnabled() {
    return apiEnabled;
  }

  public boolean isAllowLocalhostRuns() {
    return allowLocalhostRuns;
  }

  public boolean isAllowNonLoopback() {
    return allowNonLoopback;
  }

  public String getApiTokenFile() {
    return apiTokenFile;
  }

  public int getRunRateLimitPerMinute() {
    return runRateLimitPerMinute;
  }

  public int getTokenCreateRateLimitPerMinute() {
    return tokenCreateRateLimitPerMinute;
  }

  public int getTokenTtlDays() {
    return tokenTtlDays;
  }

  public int getTokenMaxTtlDays() {
    return tokenMaxTtlDays;
  }

  public String getServerAddress() {
    return serverAddress;
  }

  public int getServerPort() {
    return serverPort;
  }

  /** True when bind address is empty (all interfaces) or not a loopback host. */
  public boolean isBoundBeyondLoopback() {
    if (serverAddress == null || serverAddress.isBlank()) {
      return true;
    }
    String a = serverAddress.trim().toLowerCase();
    return !(a.equals("127.0.0.1")
        || a.equals("::1")
        || a.equals("localhost")
        || a.equals("0:0:0:0:0:0:0:1"));
  }
}
