package com.gw.web;

import com.gw.utils.BaseTool;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

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

  @Autowired private BaseTool bt;

  @Value("${geoweaver.login_required:false}")
  private boolean loginRequired;

  @Override
  public boolean preHandle(
      HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

    // Skip login check for login-related endpoints and static resources
    String requestPath = request.getRequestURI();
    String contextPath = request.getContextPath();

    // Remove context path from request path
    if (contextPath != null && !contextPath.isEmpty() && requestPath.startsWith(contextPath)) {
      requestPath = requestPath.substring(contextPath.length());
    }

    // Normalize path (ensure it starts with /)
    if (!requestPath.startsWith("/")) {
      requestPath = "/" + requestPath;
    }

    // Allow access to login page, login API, static resources, and key endpoint
    // Check exact matches and path starts with for login endpoints
    boolean isLoginEndpoint = requestPath.equals("/web/localhost-login")
        || requestPath.equals("/web/authenticateLocalhost")
        || requestPath.equals("/web/key")
        || requestPath.startsWith("/web/localhost-login?")
        || requestPath.startsWith("/web/authenticateLocalhost?")
        || requestPath.startsWith("/web/key?")
        || requestPath.endsWith("/localhost-login")
        || requestPath.endsWith("/authenticateLocalhost")
        || requestPath.endsWith("/key");
    
    if (isLoginEndpoint
        || requestPath.startsWith("/static/")
        || requestPath.startsWith("/js/")
        || requestPath.startsWith("/css/")
        || requestPath.startsWith("/images/")
        || requestPath.startsWith("/img/")
        || requestPath.startsWith("/gif/")
        || requestPath.startsWith("/fonts/")
        || requestPath.startsWith("/webfonts/")
        || requestPath.startsWith("/ssh/")
        || requestPath.endsWith(".js")
        || requestPath.endsWith(".css")
        || requestPath.endsWith(".png")
        || requestPath.endsWith(".jpg")
        || requestPath.endsWith(".jpeg")
        || requestPath.endsWith(".gif")
        || requestPath.endsWith(".ico")
        || requestPath.endsWith(".woff")
        || requestPath.endsWith(".woff2")
        || requestPath.endsWith(".ttf")
        || requestPath.endsWith(".eot")
        || requestPath.endsWith(".svg")
        || requestPath.endsWith(".otf")) {
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
            if ("true".equals(cookie.getValue())) {
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
    Cookie cookie = new Cookie(COOKIE_NAME_LOCALHOST_AUTHENTICATED, authenticated ? "true" : "false");
    cookie.setMaxAge(authenticated ? COOKIE_MAX_AGE : 0); // 30 days or delete
    cookie.setPath("/");
    cookie.setHttpOnly(true); // Prevent XSS attacks
    // Note: Secure flag should be set in production with HTTPS
    // cookie.setSecure(true);
    response.addCookie(cookie);
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
