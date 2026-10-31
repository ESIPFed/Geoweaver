package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LogRateLimitTest {

  @BeforeEach
  void clean() {
    LogRateLimit.reset();
  }

  @Test
  void writesOncePerMinuteAndCountsWhatWasLeftOut() {
    long t = 1_000_000_000L;
    assertThat(LogRateLimit.allow("k1", t)).isEmpty();
    for (int i = 1; i <= 999; i++) assertThat(LogRateLimit.allow("k1", t + i)).isNull();
    assertThat(LogRateLimit.allow("k1", t + LogRateLimit.INTERVAL_MS)).contains("999 more");
    assertThat(LogRateLimit.allow("k1", t + LogRateLimit.INTERVAL_MS + 1)).isNull();
    assertThat(LogRateLimit.allow("k2", t)).isEmpty(); // keys are independent
  }

  @Test
  void keyCountStaysBounded() {
    long t = 2_000_000_000L;
    for (int i = 0; i < 5000; i++) LogRateLimit.allow("token-" + i, t + i * LogRateLimit.INTERVAL_MS);
    assertThat(LogRateLimit.keys()).isLessThanOrEqualTo(1001);
  }
}
