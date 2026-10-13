package com.gw.utils;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Tracks which client tokens want live log lines for a given process/workflow history id.
 *
 * <p>Process execution is started with a single owner token (the browser tab that clicked Run).
 * Other tabs generate different {@code CLIENT_TOKEN}s and previously received no live feed.
 * Subscribers registered here receive a fan-out copy of each log line for that history id.
 */
@Component
public class ExecutionLogBroker {

  private static final Logger logger = LoggerFactory.getLogger(ExecutionLogBroker.class);

  /** historyId -> set of client tokens */
  private final ConcurrentHashMap<String, Set<String>> subscribers = new ConcurrentHashMap<>();

  public void subscribe(String historyId, String token) {
    if (BaseTool.isNull(historyId) || BaseTool.isNull(token)) {
      return;
    }
    subscribers
        .computeIfAbsent(historyId, k -> ConcurrentHashMap.newKeySet())
        .add(token);
    logger.info(
        "Token {} subscribed to execution logs for history {}", token, historyId);
  }

  public void unsubscribe(String historyId, String token) {
    if (BaseTool.isNull(historyId) || BaseTool.isNull(token)) {
      return;
    }
    Set<String> set = subscribers.get(historyId);
    if (set != null) {
      set.remove(token);
      if (set.isEmpty()) {
        subscribers.remove(historyId, set);
      }
    }
  }

  public void unsubscribeToken(String token) {
    if (BaseTool.isNull(token)) {
      return;
    }
    for (String historyId : subscribers.keySet()) {
      unsubscribe(historyId, token);
    }
  }

  public Set<String> getSubscribers(String historyId) {
    if (BaseTool.isNull(historyId)) {
      return Collections.emptySet();
    }
    Set<String> set = subscribers.get(historyId);
    if (set == null || set.isEmpty()) {
      return Collections.emptySet();
    }
    return Collections.unmodifiableSet(set);
  }

  /**
   * Extract history id from a log line of the form {@code historyId*_*}message.
   */
  public static String extractHistoryId(String message) {
    if (BaseTool.isNull(message)) {
      return null;
    }
    String sep = BaseTool.log_separator;
    int idx = message.indexOf(sep);
    if (idx <= 0) {
      return null;
    }
    return message.substring(0, idx);
  }
}
