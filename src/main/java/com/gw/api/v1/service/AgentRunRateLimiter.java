package com.gw.api.v1.service;

import com.gw.api.v1.AgentApiProperties;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Simple per-token sliding window rate limit for run starts. */
@Component
public class AgentRunRateLimiter {

  private final AgentApiProperties properties;
  private final ConcurrentHashMap<String, Deque<Long>> windows = new ConcurrentHashMap<>();

  public AgentRunRateLimiter(AgentApiProperties properties) {
    this.properties = properties;
  }

  /** @return true if allowed, false if over limit */
  public boolean tryAcquire(String tokenFingerprint) {
    int limit = Math.max(1, properties.getRunRateLimitPerMinute());
    long now = System.currentTimeMillis();
    long windowMs = 60_000L;
    String key = tokenFingerprint == null ? "anon" : tokenFingerprint;
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
