package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.gw.api.v1.service.AgentApiTokenService;
import jakarta.servlet.FilterChain;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AgentApiTokenFilterTest {

  @Mock AgentApiTokenService tokenService;
  @Mock FilterChain chain;

  AgentApiProperties properties;
  AgentApiTokenFilter filter;

  @BeforeEach
  void setUp() {
    properties = new AgentApiProperties();
    ReflectionTestUtils.setField(properties, "apiEnabled", true);
    filter = new AgentApiTokenFilter(properties, tokenService);
  }

  @Test
  void healthBypassesAuthEvenWhenDisabled() throws Exception {
    ReflectionTestUtils.setField(properties, "apiEnabled", false);
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/health");
    req.setContextPath("/Geoweaver");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(200);
  }

  @Test
  void disabledApiReturns503Json() throws Exception {
    ReflectionTestUtils.setField(properties, "apiEnabled", false);
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/capabilities");
    req.setContextPath("/Geoweaver");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(503);
    assertThat(res.getContentAsString(StandardCharsets.UTF_8)).contains("api_disabled");
  }

  @Test
  void missingBearerReturns401Json() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/processes");
    req.setContextPath("/Geoweaver");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(401);
    assertThat(res.getContentAsString(StandardCharsets.UTF_8)).contains("unauthorized");
  }

  @Test
  void wrongTokenReturns401() throws Exception {
    when(tokenService.matches("bad-token-value-that-is-long-enough-xxxx")).thenReturn(false);
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/processes");
    req.setContextPath("/Geoweaver");
    req.addHeader("Authorization", "Bearer bad-token-value-that-is-long-enough-xxxx");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(401);
  }

  @Test
  void postTokensBypassesBearerSoPasswordBootstrapCanRun() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/Geoweaver/api/v1/tokens");
    req.setContextPath("/Geoweaver");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    // Filter must not return 401; controller enforces password + api-enabled.
    assertThat(res.getStatus()).isEqualTo(200);
  }

  @Test
  void getTokensStillRequiresBearer() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/tokens");
    req.setContextPath("/Geoweaver");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(401);
  }

  @Test
  void optionsPreflightDoesNotRequireBearer() throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("OPTIONS", "/Geoweaver/api/v1/processes");
    req.setContextPath("/Geoweaver");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(200);
  }

  @Test
  void validBearerFromNonLoopbackAddressIsAllowedWhenNetworkAccessIsOn() throws Exception {
    ReflectionTestUtils.setField(properties, "allowNonLoopback", true);
    String token = "gwt_remote_client_token_value_xxxx";
    when(tokenService.matches(token)).thenReturn(true);
    when(tokenService.fingerprint(token)).thenReturn("deadbeef0123");
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/capabilities");
    req.setContextPath("/Geoweaver");
    req.setRemoteAddr("203.0.113.10");
    req.addHeader("Authorization", "Bearer " + token);
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(200);
  }

  @Test
  void acceptsXGeoweaverAgentTokenWhenAuthorizationMissing() throws Exception {
    String token = "gwt_ood_proxy_fallback_token_xxxx";
    when(tokenService.matches(token)).thenReturn(true);
    when(tokenService.fingerprint(token)).thenReturn("cafebabe0123");
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/capabilities");
    req.setContextPath("/Geoweaver");
    req.addHeader(AgentApiTokenFilter.AGENT_TOKEN_HEADER, token);
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(200);
  }

  @Test
  void acceptsGwAgentBearerCookieWhenAuthorizationMissing() throws Exception {
    String token = "gwt_ood_cookie_fallback_token_xxxx";
    when(tokenService.matches(token)).thenReturn(true);
    when(tokenService.fingerprint(token)).thenReturn("deadbeef9999");
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/capabilities");
    req.setContextPath("/Geoweaver");
    req.setCookies(new jakarta.servlet.http.Cookie(AgentApiTokenFilter.AGENT_TOKEN_COOKIE, token));
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    assertThat(res.getStatus()).isEqualTo(200);
  }

  private MockHttpServletResponse call(String remoteAddr, String host, String header, String value)
      throws Exception {
    MockHttpServletRequest req = new MockHttpServletRequest("POST", "/Geoweaver/api/v1/tokens");
    req.setContextPath("/Geoweaver");
    req.setRemoteAddr(remoteAddr);
    if (host != null) req.addHeader("Host", host);
    if (header != null) req.addHeader(header, value);
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    return res;
  }

  @Test
  void byDefaultOtherComputersCannotReachAnyPathNotEvenTokenCreateOrHealth() throws Exception {
    MockHttpServletResponse res = call("10.1.2.3", "gw.example.org:8070", null, null);
    assertThat(res.getStatus()).isEqualTo(403);
    assertThat(res.getContentAsString(StandardCharsets.UTF_8)).contains("local_only");

    MockHttpServletRequest health = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/health");
    health.setContextPath("/Geoweaver");
    health.setRemoteAddr("10.1.2.3");
    MockHttpServletResponse hres = new MockHttpServletResponse();
    filter.doFilter(health, hres, chain);
    assertThat(hres.getStatus()).isEqualTo(403);
  }

  @Test
  void aProgramOnThisComputerReachesTheApi() throws Exception {
    for (String host : new String[] {"127.0.0.1:8070", "localhost:8070", "localhost", "[::1]:8070", null}) {
      MockHttpServletResponse res = call("127.0.0.1", host, null, null);
      assertThat(res.getStatus()).as("Host " + host).isNotEqualTo(403);
    }
    assertThat(call("0:0:0:0:0:0:0:1", "[::1]:8070", null, null).getStatus()).isNotEqualTo(403);
  }

  @Test
  void aReverseProxyOnThisComputerIsTreatedAsTheNetwork() throws Exception {
    for (String h : new String[] {"X-Forwarded-For", "X-Real-IP", "Forwarded", "X-Forwarded-Host"}) {
      assertThat(call("127.0.0.1", "127.0.0.1:8070", h, "203.0.113.9").getStatus()).as(h).isEqualTo(403);
    }
  }

  @Test
  void aWebPageUsingDnsRebindingIsRefused() throws Exception {
    // the browser connects to 127.0.0.1 but still sends the attacker's host name
    assertThat(call("127.0.0.1", "evil.example.com:8070", null, null).getStatus()).isEqualTo(403);
    assertThat(call("127.0.0.1", "localhost.evil.example.com", null, null).getStatus()).isEqualTo(403);
    assertThat(call("127.0.0.1", "127.0.0.1.nip.io:8070", null, null).getStatus()).isEqualTo(403);
    assertThat(call("127.0.0.1", "localhost:80@evil", null, null).getStatus()).isEqualTo(403);
  }

  @Test
  void networkAccessWorksOnlyWhenSwitchedOn() throws Exception {
    ReflectionTestUtils.setField(properties, "allowNonLoopback", true);
    MockHttpServletRequest req = new MockHttpServletRequest("GET", "/Geoweaver/api/v1/processes");
    req.setContextPath("/Geoweaver");
    req.setRemoteAddr("10.1.2.3");
    req.addHeader("Host", "gw.example.org");
    MockHttpServletResponse res = new MockHttpServletResponse();
    filter.doFilter(req, res, chain);
    // passes the network check and still needs a token
    assertThat(res.getStatus()).isEqualTo(401);
  }

  @Test
  void anEncodedPathCannotSkipTheFilter() throws Exception {
    for (String uri :
        new String[] {
          "/Geoweaver/api/%761/processes", "/Geoweaver/api/v%31/tokens", "/Geoweaver/%61pi/v1/tokens",
          "/Geoweaver/API/v1/processes", "/Geoweaver/api/v1"
        }) {
      MockHttpServletRequest req = new MockHttpServletRequest("POST", uri);
      req.setContextPath("/Geoweaver");
      req.setRemoteAddr("10.1.2.3");
      MockHttpServletResponse res = new MockHttpServletResponse();
      filter.doFilter(req, res, chain);
      assertThat(res.getStatus()).as(uri).isEqualTo(403);
    }
  }
}
