package com.gw.web;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.resource.ResourceHttpRequestHandler;

/** The login check cannot be skipped with a hand-written cookie or a file-like path. */
class LoginSecurityTest {

  LoginInterceptor interceptor;
  HandlerMethod controllerRoute;

  @BeforeEach
  void setUp() throws Exception {
    interceptor = new LoginInterceptor();
    ReflectionTestUtils.setField(interceptor, "loginRequired", true);
    controllerRoute = new HandlerMethod(this, LoginSecurityTest.class.getDeclaredMethod("setUp"));
  }

  private MockHttpServletResponse get(String uri, Object handler, Cookie cookie) throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", uri);
    req.setContextPath("/Geoweaver");
    if (cookie != null) req.setCookies(cookie);
    MockHttpServletResponse res = new MockHttpServletResponse();
    boolean allowed = interceptor.preHandle(req, res, handler);
    if (allowed) res.setStatus(200);
    return res;
  }

  @Test
  void aHandWrittenCookieIsNotALogin() throws Exception {
    MockHttpServletResponse res =
        get("/Geoweaver/web/geoweaver", controllerRoute, new Cookie("gw_localhost_auth", "true"));
    assertThat(res.getStatus()).isEqualTo(302);
    assertThat(res.getRedirectedUrl()).isEqualTo("/Geoweaver/web/localhost-login");
  }

  @Test
  void aCookieThisServerSignedIsALogin() throws Exception {
    MockHttpServletResponse res =
        get("/Geoweaver/web/geoweaver", controllerRoute, new Cookie("gw_localhost_auth", AuthCookie.issue()));
    assertThat(res.getStatus()).isEqualTo(200);
  }

  @Test
  void tamperedOrExpiredCookiesAreRejected() {
    String good = AuthCookie.issue();
    String[] p = good.split("\\.");
    long later = Long.parseLong(p[1]) + 1000;
    assertThat(AuthCookie.isValid("v1." + later + "." + p[2])).isFalse();
    char last = good.charAt(good.length() - 1);
    String tampered = good.substring(0, good.length() - 1) + (last == 'A' ? 'E' : 'A');
    assertThat(AuthCookie.isValid(tampered)).isFalse();
    assertThat(AuthCookie.isValid("v1." + (Instant.now().getEpochSecond() - 10) + "." + p[2])).isFalse();
    assertThat(AuthCookie.isValid("")).isFalse();
    assertThat(AuthCookie.isValid(null)).isFalse();
  }

  @Test
  void changingThePasswordSignsEveryoneOut() throws Exception {
    Path secret = Paths.get(System.getProperty("user.home"), "geoweaver", ".secret");
    byte[] before = Files.exists(secret) ? Files.readAllBytes(secret) : null;
    try {
      Files.createDirectories(secret.getParent());
      Files.write(secret, "hash-one".getBytes(StandardCharsets.UTF_8));
      String cookie = AuthCookie.issue();
      assertThat(AuthCookie.isValid(cookie)).isTrue();
      Files.write(secret, "hash-two".getBytes(StandardCharsets.UTF_8));
      assertThat(AuthCookie.isValid(cookie)).isFalse();
    } finally {
      if (before == null) Files.deleteIfExists(secret);
      else Files.write(secret, before);
    }
  }

  @Test
  void aControllerRouteThatLooksLikeAFileStillNeedsLogin() throws Exception {
    for (String uri :
        new String[] {
          "/Geoweaver/web/results/output.png",
          "/Geoweaver/web/file/data.js",
          "/Geoweaver/web/download/temp/x.css",
          "/Geoweaver/web/download/x/key",
          "/Geoweaver/web/%67eoweaver",
        }) {
      assertThat(get(uri, controllerRoute, null).getStatus()).as(uri).isEqualTo(302);
    }
  }

  @Test
  void staticFilesAndTheLoginPageStayPublic() throws Exception {
    assertThat(get("/Geoweaver/js/gw.main.js", new ResourceHttpRequestHandler(), null).getStatus())
        .isEqualTo(200);
    assertThat(get("/Geoweaver/web/localhost-login", controllerRoute, null).getStatus()).isEqualTo(200);
    assertThat(get("/Geoweaver/web/key", controllerRoute, null).getStatus()).isEqualTo(200);
  }

  @Test
  void logoutCookieAndLoginCookieAreSetSafely() {
    MockHttpServletResponse res = new MockHttpServletResponse();
    LoginInterceptor.markAuthenticated(null, res);
    String header = res.getHeader("Set-Cookie");
    assertThat(header).startsWith("gw_localhost_auth=v1.").contains("HttpOnly").contains("SameSite=Lax");
  }
}
