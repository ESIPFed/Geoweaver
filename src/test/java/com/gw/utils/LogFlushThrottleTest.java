package com.gw.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

class LogFlushThrottleTest {

  @Test
  void flushesAtMostOncePerInterval() {
    AtomicLong now = new AtomicLong(1_000);
    LogFlushThrottle t = new LogFlushThrottle(5_000, now::get);

    assertThat(t.shouldFlush()).isFalse();
    now.set(5_999);
    assertThat(t.shouldFlush()).isFalse();
    now.set(6_000);
    assertThat(t.shouldFlush()).isTrue();
    assertThat(t.shouldFlush()).isFalse();
    now.set(11_000);
    assertThat(t.shouldFlush()).isTrue();
  }

  @Test
  void zeroIntervalFlushesEveryLine() {
    LogFlushThrottle t = new LogFlushThrottle(0, () -> 42L);
    assertThat(t.shouldFlush()).isTrue();
    assertThat(t.shouldFlush()).isTrue();
  }

  @Test
  void tenThousandLinesInTenSecondsCauseTwoWrites() {
    AtomicLong now = new AtomicLong(0);
    LogFlushThrottle t = new LogFlushThrottle(5_000, now::get);
    int writes = 0;
    for (int line = 0; line < 10_000; line++) {
      now.set(line); // one line per millisecond
      if (t.shouldFlush()) writes++;
    }
    assertThat(writes).isEqualTo(1);
    now.set(10_000);
    assertThat(t.shouldFlush()).isTrue();
  }

  @Test
  void invalidSettingFallsBackToDefault() {
    String key = "geoweaver.log.flush.ms";
    String old = System.getProperty(key);
    try {
      System.setProperty(key, "not-a-number");
      assertThat(LogFlushThrottle.configuredIntervalMs()).isEqualTo(LogFlushThrottle.DEFAULT_INTERVAL_MS);
      System.setProperty(key, "250");
      assertThat(LogFlushThrottle.configuredIntervalMs()).isEqualTo(250);
    } finally {
      if (old == null) System.clearProperty(key);
      else System.setProperty(key, old);
    }
  }
}
