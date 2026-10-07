package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.dto.TokenCreateRequest;
import com.gw.api.v1.dto.TokenCreateResponse;
import com.gw.utils.BaseTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

/**
 * Remote / password-gated Agent API token create and rotate. Always requires the GUI localhost
 * password — never uses {@code allow-localhost-runs} bypass (that flag is only for job starts).
 */
@Service
public class AgentTokenBootstrapService {

  private static final Logger logger = LoggerFactory.getLogger(AgentTokenBootstrapService.class);

  private final AgentApiProperties properties;
  private final AgentApiTokenService tokenService;
  private final AgentTokenCreateRateLimiter rateLimiter;
  private final BaseTool baseTool;

  public AgentTokenBootstrapService(
      AgentApiProperties properties,
      AgentApiTokenService tokenService,
      AgentTokenCreateRateLimiter rateLimiter,
      BaseTool baseTool) {
    this.properties = properties;
    this.tokenService = tokenService;
    this.rateLimiter = rateLimiter;
    this.baseTool = baseTool;
  }

  public TokenCreateResponse createWithLocalhostPassword(
      TokenCreateRequest body, String clientIp) {
    if (!properties.isApiEnabled()) {
      throw new ResponseStatusException(
          HttpStatus.SERVICE_UNAVAILABLE,
          "Agent API is disabled. Set geoweaver.agent.api-enabled=true");
    }
    if (!rateLimiter.tryAcquire(clientIp)) {
      throw new ResponseStatusException(
          HttpStatus.TOO_MANY_REQUESTS,
          "Too many token create attempts from this address. Try again later.");
    }
    String password = body == null ? null : body.hostPassword();
    if (BaseTool.isNull(password)) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "hostPassword is required (Geoweaver GUI localhost password on the server)");
    }
    try {
      if (!baseTool.checkLocalhostPassword(password)) {
        // Same family of failure as run password checks — do not reveal whether API token exists.
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Authentication failed. Wrong localhost password.");
      }
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      logger.warn("Localhost password check failed: {}", e.getMessage());
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Authentication failed. Wrong localhost password.");
    }

    boolean revokeOthers = body != null && Boolean.TRUE.equals(body.revokeOthers());
    Integer ttlDays = body == null ? null : body.ttlDays();
    try {
      AgentApiTokenService.IssuedToken issued = tokenService.issue(ttlDays, revokeOthers);
      logger.info(
          "Agent API token created via password bootstrap fingerprint={} expiresAt={} ttlDays={} clientIp={} revokeOthers={}",
          issued.fingerprint(),
          issued.expiresAt(),
          issued.ttlDays(),
          sanitizeIp(clientIp),
          revokeOthers);
      return new TokenCreateResponse(
          issued.token(),
          issued.fingerprint(),
          issued.expiresAt().toString(),
          issued.ttlDays(),
          "Authorization: Bearer <token>",
          revokeOthers);
    } catch (ResponseStatusException e) {
      throw e;
    } catch (Exception e) {
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Failed to store agent API token in the database");
    }
  }

  static String sanitizeIp(String ip) {
    if (ip == null) {
      return "unknown";
    }
    String cleaned = ip.replaceAll("[^0-9a-fA-F.:]", "");
    if (cleaned.isEmpty()) {
      return "unknown";
    }
    return cleaned.length() > 64 ? cleaned.substring(0, 64) : cleaned;
  }
}
