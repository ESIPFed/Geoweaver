package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiProperties;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * Sliding-window rate limit for password-gated token create (keyed by client IP). Stricter than run
 * starts because wrong-password guessing targets the localhost password.
 */
@Component
public class AgentTokenCreateRateLimiter {

  private static final long WINDOW_MS = 60_000L;

  private final AgentApiProperties properties;
  private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

  public AgentTokenCreateRateLimiter(AgentApiProperties properties) {
    this.properties = properties;
  }

  private static final String GLOBAL_KEY = "\u0000global";
  private static final int MAX_TRACKED_CLIENTS = 10_000;

  /**
   * @return true if allowed, false if over the per-IP or the all-clients limit. The global cap
   *     stops a guess spread across many source addresses.
   */
  public boolean tryAcquire(String clientKey) {
    String key = clientKey == null || clientKey.isBlank() ? "unknown" : clientKey;
    if (windows.size() > MAX_TRACKED_CLIENTS) {
      long now = System.currentTimeMillis();
      windows.entrySet().removeIf(e -> isIdle(e.getValue(), now));
    }
    return acquire(key, Math.max(1, properties.getTokenCreateRateLimitPerMinute()))
        && acquire(GLOBAL_KEY, Math.max(1, properties.getTokenCreateGlobalRateLimitPerMinute()));
  }

  private boolean acquire(String key, int limit) {
    long now = System.currentTimeMillis();
    Deque<Long> q = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
    synchronized (q) {
      while (!q.isEmpty() && now - q.peekFirst() > WINDOW_MS) {
        q.pollFirst();
      }
      if (q.size() >= limit) {
        return false;
      }
      q.addLast(now);
      return true;
    }
  }

  private static boolean isIdle(Deque<Long> q, long now) {
    synchronized (q) {
      return q.isEmpty() || now - q.peekLast() > WINDOW_MS;
    }
  }
}
