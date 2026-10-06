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
}
