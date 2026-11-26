package com.gw.web;

import com.gw.utils.BaseTool;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;
import org.springframework.web.util.UrlPathHelper;

/**
 * Interceptor to check if login is required and if user is authenticated.
 * When login_required is enabled, users must authenticate with localhost password
 * before accessing Geoweaver.
 * Uses both session and cookie to maintain login state for continuity.
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

  private static final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
  private static final String SESSION_ATTR_LOCALHOST_AUTHENTICATED = "localhost_authenticated";
  private static final String COOKIE_NAME_LOCALHOST_AUTHENTICATED = "gw_localhost_auth";
  private static final int COOKIE_MAX_AGE = 30 * 24 * 60 * 60; // 30 days in seconds
  private static final UrlPathHelper PATHS = new UrlPathHelper();

  @Autowired private BaseTool bt;

  @Value("${geoweaver.login_required:false}")
  private boolean loginRequired;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    // Decoded path inside the app, the same path Spring uses to pick the handler
    // (so %-encoded spellings cannot dodge the checks below).
    String requestPath = PATHS.getPathWithinApplication(request);
    String contextPath = request.getContextPath();
    if (requestPath == null || !requestPath.startsWith("/")) {
      requestPath = "/" + (requestPath == null ? "" : requestPath);
    }

    // Login page, login call and the public key the login page encrypts the password with
    boolean isLoginEndpoint = requestPath.equals("/web/localhost-login")
        || requestPath.equals("/web/authenticateLocalhost")
        || requestPath.equals("/web/key");

    // Agent API uses Bearer token + JSON errors (AgentApiTokenFilter). Never HTML-redirect.
    if (requestPath.startsWith("/api/v1/") || requestPath.equals("/api/v1")) {
      return true;
    }

    // Agent API OpenAPI schema only (exact path; the full /v3/api-docs stays behind login).
    if (requestPath.equals("/v3/api-docs/agent-api")) {
      return true;
    }

    // One-time Agent API token copy page (loopback CLI reveal on port 8070).
    if (requestPath.startsWith("/agent-token-reveal/")) {
      return true;
    }

    // Only real static files are public; a controller route never is, whatever its name ends with.
    if (isLoginEndpoint || handler instanceof ResourceHttpRequestHandler) {
      return true;
    }

    // If login is not required, allow all requests
    if (!loginRequired) {
      return true;
    }

    // Check authentication via session or cookie
    HttpSession session = request.getSession(false);
    boolean authenticated = false;
    
    // First check session
    if (session != null) {
      Boolean sessionAuth =
          (Boolean) session.getAttribute(SESSION_ATTR_LOCALHOST_AUTHENTICATED);
      authenticated = (sessionAuth != null && sessionAuth);
    }
    
    // If not authenticated via session, check cookie
    if (!authenticated) {
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie : cookies) {
          if (COOKIE_NAME_LOCALHOST_AUTHENTICATED.equals(cookie.getName())) {
            if (AuthCookie.isValid(cookie.getValue())) {
              authenticated = true;
              // Restore session authentication from cookie
              if (session == null) {
                session = request.getSession(true);
              }
              session.setAttribute(SESSION_ATTR_LOCALHOST_AUTHENTICATED, true);
              // Refresh cookie to extend expiration
              setAuthCookie(response, true);
              logger.debug("Restored authentication from cookie");
              break;
            }
          }
        }
      }
    }
    
    if (!authenticated) {
      logger.debug("User not authenticated, redirecting to login");
      response.sendRedirect(contextPath + "/web/localhost-login");
      return false;
    }

    return true;
  }

  /**
   * Mark session as authenticated and set persistent cookie
   */
  public static void markAuthenticated(HttpSession session, HttpServletResponse response) {
    if (session != null) {
      session.setAttribute(SESSION_ATTR_LOCALHOST_AUTHENTICATED, true);
      // Set very long session timeout (30 days)
      session.setMaxInactiveInterval(COOKIE_MAX_AGE);
    }
    if (response != null) {
      setAuthCookie(response, true);
    }
  }

  /**
   * Mark session as authenticated (without response, for backward compatibility)
   */
  public static void markAuthenticated(HttpSession session) {
    if (session != null) {
      session.setAttribute(SESSION_ATTR_LOCALHOST_AUTHENTICATED, true);
      session.setMaxInactiveInterval(COOKIE_MAX_AGE);
    }
  }

  /**
   * Set authentication cookie
   */
  private static void setAuthCookie(HttpServletResponse response, boolean authenticated) {
    // Signed value (see AuthCookie): a hand-written cookie is not a login.
    ResponseCookie cookie =
        ResponseCookie.from(COOKIE_NAME_LOCALHOST_AUTHENTICATED, authenticated ? AuthCookie.issue() : "")
            .maxAge(authenticated ? COOKIE_MAX_AGE : 0)
            .path("/")
            .httpOnly(true)
            .sameSite("Lax")
            .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }

  /** True when the request carries a login cookie this server signed. */
  public static boolean hasValidAuthCookie(HttpServletRequest request) {
    Cookie[] cookies = request.getCookies();
    if (cookies == null) return false;
    for (Cookie c : cookies) {
      if (COOKIE_NAME_LOCALHOST_AUTHENTICATED.equals(c.getName()) && AuthCookie.isValid(c.getValue())) {
        return true;
      }
    }
    return false;
  }

  /**
   * Check if session is authenticated
   */
  public static boolean isAuthenticated(HttpSession session) {
    if (session == null) {
      return false;
    }
    Boolean authenticated =
        (Boolean) session.getAttribute(SESSION_ATTR_LOCALHOST_AUTHENTICATED);
    return authenticated != null && authenticated;
  }

  /**
   * Clear authentication (logout)
   */
  public static void clearAuthentication(HttpSession session, HttpServletResponse response) {
    if (session != null) {
      session.removeAttribute(SESSION_ATTR_LOCALHOST_AUTHENTICATED);
    }
    if (response != null) {
      Cookie cookie = new Cookie(COOKIE_NAME_LOCALHOST_AUTHENTICATED, "");
      cookie.setMaxAge(0);
      cookie.setPath("/");
      response.addCookie(cookie);
    }
  }
}
