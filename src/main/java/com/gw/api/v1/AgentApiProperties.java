package com.gw.api.v1;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** Configuration for the Geoweaver Agent HTTP API (`/api/v1`). */
@Component
public class AgentApiProperties {

  @Value("${geoweaver.agent.api-enabled:true}")
  private boolean apiEnabled;

  @Value("${geoweaver.agent.allow-localhost-runs:true}")
  private boolean allowLocalhostRuns;

  @Value("${geoweaver.agent.allow-non-loopback:true}")
  private boolean allowNonLoopback;

  /**
   * Required when the Agent API is enabled and the JVM bind is beyond loopback. Defaults to true
   * with the other agent defaults so Open OnDemand / all-interface binds can start; operators still
   * see SECURITY WARN logs. Set false only when intentionally fail-closing non-loopback API binds.
   */
  @Value("${geoweaver.agent.acknowledge-network-risk:true}")
  private boolean acknowledgeNetworkRisk;

  /** Fan-out workspace change events to open GUI browsers (independent of api-enabled). */
  @Value("${geoweaver.agent.workspace-events-enabled:true}")
  private boolean workspaceEventsEnabled;

  @Value("${geoweaver.agent.allow-delete:true}")
  private boolean allowDelete;

  /**
   * When false, the Agent API cannot add/edit/delete hosts or run on remote SSH hosts (localhost
   * only). Listing hosts stays available.
   */
  @Value("${geoweaver.agent.allow-remote-hosts:true}")
  private boolean allowRemoteHosts;

  @Value("${geoweaver.agent.api-token-file:${user.home}/gw-workspace/.agent_api_token}")
  private String apiTokenFile;

  @Value("${geoweaver.agent.run-rate-limit-per-minute:30}")
  private int runRateLimitPerMinute;

  /** Password-gated remote token create attempts per client IP per minute. */
  @Value("${geoweaver.agent.token-create-rate-limit-per-minute:5}")
  private int tokenCreateRateLimitPerMinute;

  /** Password-gated token create attempts per minute from all clients together. */
  @Value("${geoweaver.agent.token-create-global-rate-limit-per-minute:30}")
  private int tokenCreateGlobalRateLimitPerMinute;

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

  public boolean isAcknowledgeNetworkRisk() {
    return acknowledgeNetworkRisk;
  }

  public boolean isWorkspaceEventsEnabled() {
    return workspaceEventsEnabled;
  }

  /** When false, DELETE on processes/workflows returns 403 (create/edit/run still allowed). */
  public boolean isAllowDelete() {
    return allowDelete;
  }

  public boolean isAllowRemoteHosts() {
    return allowRemoteHosts;
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

  public int getTokenCreateGlobalRateLimitPerMinute() {
    return tokenCreateGlobalRateLimitPerMinute;
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
    return !isLoopbackAddress(serverAddress);
  }

  /** IPv4/IPv6 loopback literals as Tomcat reports them, plus {@code localhost}. */
  public static boolean isLoopbackAddress(String address) {
    if (address == null) {
      return false;
    }
    String a = address.trim().toLowerCase();
    return a.equals("127.0.0.1")
        || a.equals("::1")
        || a.equals("0:0:0:0:0:0:0:1")
        || a.equals("localhost");
  }
}
