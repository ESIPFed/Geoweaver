package com.gw.api.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.api.v1.dto.AgentErrorResponse;
import com.gw.api.v1.service.AgentApiTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Enforces Bearer token auth for {@code /api/v1/**} except public health. Returns JSON errors only.
 *
 * <p>Open OnDemand / some reverse proxies strip the {@code Authorization} header. Agents may also
 * send the raw token in {@code X-Geoweaver-Agent-Token} or cookie {@code GW_AGENT_BEARER} (same
 * secret as Bearer). Prefer HTTPS.
 */
@Component
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@Order(Ordered.HIGHEST_PRECEDENCE + 20)
public class AgentApiTokenFilter extends OncePerRequestFilter {

  /** Alternate header when proxies drop {@code Authorization}. */
  public static final String AGENT_TOKEN_HEADER = "X-Geoweaver-Agent-Token";

  /** Alternate cookie name when proxies drop {@code Authorization}. */
  public static final String AGENT_TOKEN_COOKIE = "GW_AGENT_BEARER";

  private static final Logger logger = LoggerFactory.getLogger(AgentApiTokenFilter.class);

  private final AgentApiProperties properties;
  private final AgentApiTokenService tokenService;
  private final ObjectMapper objectMapper = new ObjectMapper();

  public AgentApiTokenFilter(AgentApiProperties properties, AgentApiTokenService tokenService) {
    this.properties = properties;
    this.tokenService = tokenService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = normalizedPath(request);
    return !path.startsWith("/api/v1/");
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    String path = normalizedPath(request);
    try {
      if (path.equals("/api/v1/health") || path.startsWith("/api/v1/health?")) {
        filterChain.doFilter(request, response);
        return;
      }

      // Browser on another origin sends OPTIONS before GET/POST. No secrets in preflight.
      if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
        filterChain.doFilter(request, response);
        return;
      }

      // Password-gated bootstrap: mint/rotate Bearer token without an existing token.
      // Controller still requires api-enabled + GUI localhost password + IP rate limit.
      if (isTokenCreatePath(request, path)) {
        filterChain.doFilter(request, response);
        return;
      }

      // Loopback-only one-time browser reveal (controller enforces 127.0.0.1).
      if (isTokenRevealPath(path)) {
        filterChain.doFilter(request, response);
        return;
      }

      if (!properties.isApiEnabled()) {
        writeJson(response, 503, "api_disabled", "Agent API is disabled. Set geoweaver.agent.api-enabled=true");
        return;
      }

      String token = extractBearer(request);
      if (token == null || !tokenService.matches(token)) {
        writeJson(response, 401, "unauthorized", "Missing or invalid Authorization Bearer token");
        return;
      }

      String fp = tokenService.fingerprint(token);
      AgentApiAuthContext.setTokenFingerprint(fp);
      logger.debug("Agent API authenticated fingerprint={}", fp);
      filterChain.doFilter(request, response);
    } finally {
      AgentApiAuthContext.clear();
    }
  }

  static boolean isTokenCreatePath(HttpServletRequest request, String path) {
    if (!"POST".equalsIgnoreCase(request.getMethod())) {
      return false;
    }
    return path.equals("/api/v1/tokens") || path.startsWith("/api/v1/tokens?");
  }

  static boolean isTokenRevealPath(String path) {
    return path.startsWith("/api/v1/token-reveal/");
  }

  static String extractBearer(HttpServletRequest request) {
    String header = request.getHeader("Authorization");
    if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
      String t = header.substring(7).trim();
      if (!t.isEmpty()) {
        return t;
      }
    }
    // Proxies (e.g. some Open OnDemand Apache setups) may strip Authorization.
    String alt = request.getHeader(AGENT_TOKEN_HEADER);
    if (alt != null) {
      String t = alt.trim();
      if (t.regionMatches(true, 0, "Bearer ", 0, 7)) {
        t = t.substring(7).trim();
      }
      if (!t.isEmpty()) {
        return t;
      }
    }
    Cookie[] cookies = request.getCookies();
    if (cookies != null) {
      for (Cookie c : cookies) {
        if (c != null && AGENT_TOKEN_COOKIE.equals(c.getName()) && c.getValue() != null) {
          String t = c.getValue().trim();
          if (!t.isEmpty()) {
            return t;
          }
        }
      }
    }
    return null;
  }

  static String normalizedPath(HttpServletRequest request) {
    String requestPath = request.getRequestURI();
    String contextPath = request.getContextPath();
    if (contextPath != null && !contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
      requestPath = requestPath.substring(contextPath.length());
    }
    if (!requestPath.startsWith("/")) {
      requestPath = "/" + requestPath;
    }
    return requestPath;
  }

  private void writeJson(HttpServletResponse response, int status, String error, String message)
      throws IOException {
    response.setStatus(status);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), new AgentErrorResponse(error, message));
  }
}
