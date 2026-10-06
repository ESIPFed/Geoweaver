package com.gw.api.v1.service;

import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** In-memory map of clientRunKey → historyId for idempotent agent runs. */
@Component
public class AgentRunIdempotencyStore {

  private final ConcurrentHashMap<String, String> keys = new ConcurrentHashMap<>();

  public String lookup(String tokenFingerprint, String clientRunKey) {
    if (clientRunKey == null || clientRunKey.isBlank()) {
      return null;
    }
    return keys.get(compose(tokenFingerprint, clientRunKey));
  }

  public void put(String tokenFingerprint, String clientRunKey, String historyId) {
    if (clientRunKey == null || clientRunKey.isBlank() || historyId == null) {
      return;
    }
    keys.put(compose(tokenFingerprint, clientRunKey), historyId);
  }

  private static String compose(String fp, String key) {
    return (fp == null ? "anon" : fp) + "::" + key.trim();
  }
}
