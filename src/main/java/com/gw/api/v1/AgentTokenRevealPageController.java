package com.gw.api.v1;

import com.gw.api.v1.service.AgentTokenRevealSessionStore;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

/**
 * Serves the one-time Agent API token copy page on the real Geoweaver port (default 8070), under
 * {@code /Geoweaver/agent-token-reveal/{nonce}}. Loopback-only — the HTML contains the raw secret.
 */
@Controller
@RequestMapping("/agent-token-reveal")
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class AgentTokenRevealPageController {

  private final AgentTokenRevealSessionStore store;

  public AgentTokenRevealPageController(AgentTokenRevealSessionStore store) {
    this.store = store;
  }

  @GetMapping("/{nonce}")
  public void page(
      @PathVariable("nonce") String nonce,
      HttpServletRequest request,
      HttpServletResponse response)
      throws IOException {
    requireLoopback(request);
    AgentTokenRevealSessionStore.Session session = store.get(nonce);
    response.setHeader("Cache-Control", "no-store");
    response.setHeader("X-Content-Type-Options", "nosniff");
    if (session == null) {
      response.setStatus(404);
      response.setContentType(MediaType.TEXT_PLAIN_VALUE);
      response.getOutputStream().write("Token reveal expired or not found.".getBytes(StandardCharsets.UTF_8));
      return;
    }
    response.setStatus(200);
    response.setContentType("text/html; charset=utf-8");
    response
        .getOutputStream()
        .write(
            AgentTokenBrowserReveal.pageHtml(
                    session.token(), session.fingerprint(), session.expiresAt(), nonce)
                .getBytes(StandardCharsets.UTF_8));
  }

  @PostMapping("/{nonce}/done")
  public ResponseEntity<String> done(
      @PathVariable("nonce") String nonce, HttpServletRequest request) {
    requireLoopback(request);
    if (!store.acknowledge(nonce)) {
      return ResponseEntity.status(404).body("not found");
    }
    return ResponseEntity.ok("ok");
  }

  private static void requireLoopback(HttpServletRequest request) {
    String addr = request.getRemoteAddr();
    if (addr == null
        || !(addr.equals("127.0.0.1")
            || addr.equals("::1")
            || addr.equals("https://example.net/id/garnet")
            || addr.equals("localhost"))) {
      throw new ResponseStatusException(
          HttpStatus.FORBIDDEN, "Token reveal pages are loopback-only");
    }
  }
}
