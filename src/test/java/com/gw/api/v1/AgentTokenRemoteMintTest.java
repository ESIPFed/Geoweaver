package com.gw.api.v1;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class AgentTokenRemoteMintTest {

  @Test
  void normalizeStripsTrailingSlash() {
    assertThat(AgentTokenRemoteMint.normalizeBaseUrl("https://gw.example.org/Geoweaver/"))
        .isEqualTo("https://gw.example.org/Geoweaver");
  }

  @Test
  void normalizeRejectsBlank() {
    assertThatThrownBy(() -> AgentTokenRemoteMint.normalizeBaseUrl("  "))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void safeErrorRedactsTokenShapedStrings() {
    assertThat(AgentTokenRemoteMint.safeError("oops gwt_0123456789abcdef leaked"))
        .contains("gwt_[redacted]")
        .doesNotContain("0123456789abcdef");
  }
}
