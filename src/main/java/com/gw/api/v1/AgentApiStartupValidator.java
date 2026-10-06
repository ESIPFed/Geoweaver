package com.gw.api.v1;

import com.gw.api.v1.service.AgentApiTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.stereotype.Component;

/** Fail-closed when agent API is enabled on a non-loopback bind without explicit allow. */
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
    if (properties.isBoundBeyondLoopback() && !properties.isAllowNonLoopback()) {
      throw new IllegalStateException(
          "geoweaver.agent.api-enabled=true but server.address is not loopback ("
              + (properties.getServerAddress() == null || properties.getServerAddress().isBlank()
                  ? "all interfaces"
                  : properties.getServerAddress())
              + "). Set server.address=127.0.0.1 or geoweaver.agent.allow-non-loopback=true "
              + "(insecure if the token leaks).");
    }
    if (properties.isBoundBeyondLoopback() && properties.isAllowNonLoopback()) {
      logger.warn(
          "SECURITY: Agent API enabled on non-loopback bind with allow-non-loopback=true. "
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
  }
}
