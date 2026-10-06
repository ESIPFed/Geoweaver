package com.gw.api.v1;

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
}
