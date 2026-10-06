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

  private final AgentApiProperties properties;
  private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

  public AgentTokenCreateRateLimiter(AgentApiProperties properties) {
    this.properties = properties;
  }

  /** @return true if allowed, false if over limit */
  public boolean tryAcquire(String clientKey) {
    int limit = Math.max(1, properties.getTokenCreateRateLimitPerMinute());
    long now = System.currentTimeMillis();
    long windowMs = 60_000L;
    String key = clientKey == null || clientKey.isBlank() ? "unknown" : clientKey;
    Deque<Long> q = windows.computeIfAbsent(key, k -> new ArrayDeque<>());
    synchronized (q) {
      while (!q.isEmpty() && now - q.peekFirst() > windowMs) {
        q.pollFirst();
      }
      if (q.size() >= limit) {
        return false;
      }
      q.addLast(now);
      return true;
    }
  }
}
