package com.gw.utils;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Lets a message that can repeat for every line of process output be written to the server log at
 * most once a minute per key, with the number of repeats left out. Without this, one busy run
 * whose output nobody is watching can write gigabytes of identical warnings to the server log.
 */
public final class LogRateLimit {

  static final long INTERVAL_MS = 60_000;
  private static final int MAX_KEYS = 1000;

  private record Slot(long since, AtomicLong suppressed) {}

  private static final Map<String, Slot> SLOTS = new ConcurrentHashMap<>();

  private LogRateLimit() {}

  /**
   * Returns null when the message should be left out, otherwise a suffix to append: empty, or a
   * note with how many identical messages were left out since the last one written.
   */
  public static String allow(String key) {
    return allow(key, System.currentTimeMillis());
  }

  static String allow(String key, long now) {
    if (SLOTS.size() >= MAX_KEYS) SLOTS.entrySet().removeIf(e -> now - e.getValue().since() >= INTERVAL_MS);
    String[] result = {null};
    SLOTS.compute(key, (k, slot) -> {
      if (slot != null && now - slot.since() < INTERVAL_MS) {
        slot.suppressed().incrementAndGet();
        return slot;
      }
      long n = slot == null ? 0 : slot.suppressed().get();
      result[0] = n == 0 ? "" : " (" + n + " more like this in the last minute)";
      return new Slot(now, new AtomicLong());
    });
    return result[0];
  }

  static void reset() {
    SLOTS.clear();
  }

  static int keys() {
    return SLOTS.size();
  }
}
