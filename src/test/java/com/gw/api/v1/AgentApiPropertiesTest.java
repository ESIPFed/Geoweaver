package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AgentApiPropertiesTest {

  @Test
  void loopbackDetection() {
    AgentApiProperties p = new AgentApiProperties();
    ReflectionTestUtils.setField(p, "serverAddress", "");
    assertThat(p.isBoundBeyondLoopback()).isTrue();

    ReflectionTestUtils.setField(p, "serverAddress", "127.0.0.1");
    assertThat(p.isBoundBeyondLoopback()).isFalse();

    ReflectionTestUtils.setField(p, "serverAddress", "0.0.0.0");
    assertThat(p.isBoundBeyondLoopback()).isTrue();
  }
}
