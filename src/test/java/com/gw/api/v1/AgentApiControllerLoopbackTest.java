package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.server.ResponseStatusException;

class AgentApiControllerLoopbackTest {

  @Test
  void revealAllowsLoopback() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRemoteAddr("127.0.0.1");
    assertThatCode(() -> AgentApiController.requireLoopback(req)).doesNotThrowAnyException();
  }

  @Test
  void revealRejectsAnotherMachine() {
    MockHttpServletRequest req = new MockHttpServletRequest();
    req.setRemoteAddr("203.0.113.10");
    assertThatThrownBy(() -> AgentApiController.requireLoopback(req))
        .isInstanceOf(ResponseStatusException.class)
        .hasMessageContaining("loopback-only");
  }
}
