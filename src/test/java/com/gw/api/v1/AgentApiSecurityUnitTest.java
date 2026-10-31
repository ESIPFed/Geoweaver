package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gw.api.v1.service.AgentTokenCreateRateLimiter;
import com.gw.utils.InternalRunCredential;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AgentApiSecurityUnitTest {

  @Test
  @DisplayName("token mint is capped across all client addresses, not only per address")
  void globalMintLimit() {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.getTokenCreateRateLimitPerMinute()).thenReturn(100);
    when(props.getTokenCreateGlobalRateLimitPerMinute()).thenReturn(3);
    AgentTokenCreateRateLimiter limiter = new AgentTokenCreateRateLimiter(props);

    assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
    assertThat(limiter.tryAcquire("10.0.0.2")).isTrue();
    assertThat(limiter.tryAcquire("10.0.0.3")).isTrue();
    assertThat(limiter.tryAcquire("10.0.0.4")).isFalse();
  }

  @Test
  @DisplayName("IPv4/IPv6 loopback literals are recognized; nothing else is")
  void loopback() {
    assertThat(AgentApiProperties.isLoopbackAddress("127.0.0.1")).isTrue();
    assertThat(AgentApiProperties.isLoopbackAddress("0:0:0:0:0:0:0:1")).isTrue();
    assertThat(AgentApiProperties.isLoopbackAddress("::1")).isTrue();
    assertThat(AgentApiProperties.isLoopbackAddress("10.1.2.3")).isFalse();
    assertThat(AgentApiProperties.isLoopbackAddress(null)).isFalse();
  }

  @Test
  @DisplayName("the in-process run credential matches only itself")
  void internalCredential() {
    assertThat(InternalRunCredential.matches(InternalRunCredential.get())).isTrue();
    assertThat(InternalRunCredential.matches("")).isFalse();
    assertThat(InternalRunCredential.matches(null)).isFalse();
    assertThat(InternalRunCredential.matches(InternalRunCredential.get() + "x")).isFalse();
  }
}
