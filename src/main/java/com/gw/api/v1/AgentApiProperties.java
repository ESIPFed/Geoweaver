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

  /**
   * False (default): the Agent API answers only programs on this computer (see {@link
   * #isLocalRequest}). True: other computers, reverse proxies and Open OnDemand may call it too.
   */
  @Value("${geoweaver.agent.allow-non-loopback:false}")
  private boolean allowNonLoopback;

  /**
   * Must also be true when {@code allow-non-loopback=true}, or Geoweaver refuses to start: a second
   * explicit statement that the operator accepts network access to an API that can run code.
   */
  @Value("${geoweaver.agent.acknowledge-network-risk:false}")
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
        || a.equals("::ffff:127.0.0.1")
        || a.equals("localhost");
  }

  /**
   * True only for a request made by a program on this computer, straight to Geoweaver:
   *
   * <ul>
   *   <li>the connection comes from a loopback address;
   *   <li>no proxy header is present (a reverse proxy on this computer forwards other people's
   *       requests, so its connection being local proves nothing);
   *   <li>the Host header names a loopback address, so a web page in the user's browser cannot reach
   *       the API by pointing its own domain name at 127.0.0.1 (DNS rebinding).
   * </ul>
   */
  public static boolean isLocalRequest(jakarta.servlet.http.HttpServletRequest request) {
    if (request == null || !isLoopbackAddress(request.getRemoteAddr())) {
      return false;
    }
    for (String h : new String[] {"X-Forwarded-For", "X-Forwarded-Host", "X-Real-IP", "Forwarded"}) {
      if (request.getHeader(h) != null) {
        return false;
      }
    }
    return isLoopbackHost(request.getHeader("Host"));
  }

  /** {@code localhost}, {@code 127.0.0.1} or {@code [::1]}, with or without a port. */
  static boolean isLoopbackHost(String host) {
    if (host == null) {
      return true; // HTTP/1.0 clients such as old curl builds; browsers always send Host
    }
    String h = host.trim().toLowerCase();
    if (h.startsWith("[")) {
      int end = h.indexOf(']');
      if (end < 0) return false;
      String rest = h.substring(end + 1);
      if (!rest.isEmpty() && !rest.matches(":[0-9]{1,5}")) return false;
      h = h.substring(1, end);
    } else {
      int colon = h.indexOf(':');
      if (colon >= 0) {
        if (!h.substring(colon).matches(":[0-9]{1,5}")) return false;
        h = h.substring(0, colon);
      }
    }
    if (h.endsWith(".")) h = h.substring(0, h.length() - 1);
    return h.equals("localhost") || h.equals("127.0.0.1") || h.equals("::1");
  }
}
