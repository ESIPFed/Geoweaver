package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.gw.api.v1.service.AgentApiTokenService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class AgentApiStartupValidatorTest {

  @Test
  void failsClosedOnNonLoopbackWithoutAllow() {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.isApiEnabled()).thenReturn(true);
    when(props.isBoundBeyondLoopback()).thenReturn(true);
    when(props.isAllowNonLoopback()).thenReturn(false);
    when(props.getServerAddress()).thenReturn("0.0.0.0");
    AgentApiTokenService tokens = mock(AgentApiTokenService.class);
    AgentApiStartupValidator v = new AgentApiStartupValidator(props, tokens);
    assertThatThrownBy(() -> v.run(new DefaultApplicationArguments(new String[0])))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("loopback");
  }

  @Test
  void failsClosedOnNonLoopbackWithoutNetworkRiskAck() {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.isApiEnabled()).thenReturn(true);
    when(props.isBoundBeyondLoopback()).thenReturn(true);
    when(props.isAllowNonLoopback()).thenReturn(true);
    when(props.isAcknowledgeNetworkRisk()).thenReturn(false);
    when(props.getServerAddress()).thenReturn("0.0.0.0");
    AgentApiTokenService tokens = mock(AgentApiTokenService.class);
    when(tokens.hasActiveToken()).thenReturn(true);
    AgentApiStartupValidator v = new AgentApiStartupValidator(props, tokens);
    assertThatThrownBy(() -> v.run(new DefaultApplicationArguments(new String[0])))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("acknowledge-network-risk");
  }

  @Test
  void startsWhenNonLoopbackAllowedAndAcknowledged() {
    AgentApiProperties props = mock(AgentApiProperties.class);
    when(props.isApiEnabled()).thenReturn(true);
    when(props.isBoundBeyondLoopback()).thenReturn(true);
    when(props.isAllowNonLoopback()).thenReturn(true);
    when(props.isAcknowledgeNetworkRisk()).thenReturn(true);
    when(props.isAllowLocalhostRuns()).thenReturn(true);
    when(props.isWorkspaceEventsEnabled()).thenReturn(true);
    when(props.getServerAddress()).thenReturn("0.0.0.0");
    AgentApiTokenService tokens = mock(AgentApiTokenService.class);
    when(tokens.hasActiveToken()).thenReturn(true);
    AgentApiStartupValidator v = new AgentApiStartupValidator(props, tokens);
    assertThatCode(() -> v.run(new DefaultApplicationArguments(new String[0])))
        .doesNotThrowAnyException();
  }
}
