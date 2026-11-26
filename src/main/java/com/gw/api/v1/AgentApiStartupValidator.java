package com.gw.api.v1;

import com.gw.api.v1.service.AgentApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

/**
 * Refuses to start when network access to the Agent API is switched on without the explicit risk
 * acknowledgment. Always logs a SECURITY summary when the API is enabled.
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AgentApiStartupValidator implements ApplicationRunner {

  private static final Logger logger = LoggerFactory.getLogger(AgentApiStartupValidator.class);

  private final AgentApiProperties properties;
  private final AgentApiTokenService tokenService;

  public AgentApiStartupValidator(AgentApiProperties properties, AgentApiTokenService tokenService) {
    this.properties = properties;
    this.tokenService = tokenService;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!properties.isApiEnabled()) {
      return;
    }

    String bindLabel =
        properties.getServerAddress() == null || properties.getServerAddress().isBlank()
            ? "all interfaces"
            : properties.getServerAddress();

    if (properties.isAllowNonLoopback() && !properties.isAcknowledgeNetworkRisk()) {
      throw new IllegalStateException(
          "geoweaver.agent.allow-non-loopback=true lets other computers use the Agent API, which can"
              + " run code on this server. Also set geoweaver.agent.acknowledge-network-risk=true to"
              + " confirm, or remove allow-non-loopback to keep the API on this computer only"
              + " (agents can still reach it over SSH at http://127.0.0.1:<port>).");
    }

    logger.warn(
        "SECURITY SUMMARY — Geoweaver Agent API (/api/v1) is ENABLED.\n"
            + "  - A valid Bearer token can create/edit python|shell processes and workflows and start"
            + " server-local runs (hostId=100001).\n"
            + "  - allow-localhost-runs={}: token may start localhost jobs without the GUI localhost"
            + " password.\n"
            + "  - allow-non-loopback={} (false = only programs on this computer) bind={}.\n"
            + "  - Harden: set geoweaver.agent.api-enabled=false, and/or bind 127.0.0.1, and/or revoke"
            + " tokens (DELETE /api/v1/tokens/{{fingerprint}}). Prefer HTTPS. Never commit tokens.",
        properties.isAllowLocalhostRuns(),
        properties.isAllowNonLoopback(),
        bindLabel);

    if (properties.isAllowNonLoopback()) {
      logger.warn(
          "SECURITY: Agent API accepts requests from other computers (allow-non-loopback=true). "
              + "Prefer HTTPS (reverse proxy to 127.0.0.1). A leaked Bearer token is full Agent API access.");
    }
    if (!tokenService.hasActiveToken()) {
      logger.warn(
          "Agent API enabled but no active token in the database. Run: gw agent-token --create "
              + "(or POST /api/v1/tokens with the GUI localhost password).");
    }
    if (properties.isAllowLocalhostRuns()) {
      logger.warn(
          "geoweaver.agent.allow-localhost-runs=true: a valid API token may start localhost jobs "
              + "without the GUI localhost password.");
    }
    if (properties.isWorkspaceEventsEnabled()) {
      logger.info(
          "Agent workspace events enabled: GUI browsers subscribed to /workspace-socket will refresh"
              + " process/workflow lists when agents mutate objects.");
    }
  }
}
