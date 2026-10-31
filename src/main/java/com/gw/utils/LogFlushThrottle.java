package com.gw.utils;

import java.util.function.LongSupplier;

/**
 * Limits how often the log of a running process is written to the database.
 *
 * <p>Each write stores the whole log collected so far, so writing on every output line costs
 * O(n^2) bytes for an n-line log and leaves the old copies behind as garbage in the H2 file. Live
 * output still goes to the browser line by line; only the database copy is batched. Final status
 * writes (Done, Failed, Stopped) are not throttled by callers.
 *
 * <p>The interval comes from the {@code GEOWEAVER_LOG_FLUSH_MS} environment variable or the
 * {@code geoweaver.log.flush.ms} system property (default 5000). A value of 0 writes every line.
 */
public class LogFlushThrottle {

  public static final long DEFAULT_INTERVAL_MS = 5000;

  private final long intervalMs;
  private final LongSupplier clock;
  private long lastFlush;

  public LogFlushThrottle() {
    this(configuredIntervalMs(), System::currentTimeMillis);
  }

  public LogFlushThrottle(long intervalMs, LongSupplier clock) {
    this.intervalMs = Math.max(0, intervalMs);
    this.clock = clock;
    this.lastFlush = clock.getAsLong();
  }

  /** Returns true when enough time has passed since the last flush, and records this flush. */
  public synchronized boolean shouldFlush() {
    long now = clock.getAsLong();
    if (intervalMs == 0 || now - lastFlush >= intervalMs) {
      lastFlush = now;
      return true;
    }
    return false;
  }

  public long getIntervalMs() {
    return intervalMs;
  }

  static long configuredIntervalMs() {
    String value = System.getProperty("geoweaver.log.flush.ms");
    if (value == null || value.isBlank()) value = System.getenv("GEOWEAVER_LOG_FLUSH_MS");
    if (value == null || value.isBlank()) return DEFAULT_INTERVAL_MS;
    try {
      return Math.max(0, Long.parseLong(value.trim()));
    } catch (NumberFormatException e) {
      return DEFAULT_INTERVAL_MS;
    }
  }
}
