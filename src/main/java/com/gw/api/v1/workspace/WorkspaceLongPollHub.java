package com.gw.api.v1.workspace;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * Dedicated long-poll hub for workspace collaboration events. Separates inventory sync from the
 * run-log {@code /api/longpoll} channel so messages are not mixed.
 */
@Component
public class WorkspaceLongPollHub {

  private static final Logger logger = LoggerFactory.getLogger(WorkspaceLongPollHub.class);
  private static final long TIMEOUT_MS = 25_000L;
  private static final int MAX_QUEUE = 200;

  private final Set<String> subscribers = ConcurrentHashMap.newKeySet();
  private final Map<String, Queue<String>> queues = new ConcurrentHashMap<>();
  private final Map<String, DeferredResult<ResponseEntity<?>>> pending = new ConcurrentHashMap<>();

  public void subscribe(String clientToken) {
    if (clientToken == null || clientToken.isBlank()) {
      return;
    }
    subscribers.add(clientToken.trim());
    queues.computeIfAbsent(clientToken.trim(), k -> new ConcurrentLinkedQueue<>());
  }

  public void unsubscribe(String clientToken) {
    if (clientToken == null) {
      return;
    }
    subscribers.remove(clientToken);
    queues.remove(clientToken);
    DeferredResult<ResponseEntity<?>> dr = pending.remove(clientToken);
    if (dr != null && !dr.isSetOrExpired()) {
      dr.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
    }
  }

  public DeferredResult<ResponseEntity<?>> poll(String clientToken) {
    DeferredResult<ResponseEntity<?>> result = new DeferredResult<>(TIMEOUT_MS);
    if (clientToken == null || clientToken.isBlank()) {
      result.setResult(ResponseEntity.badRequest().body("token required"));
      return result;
    }
    subscribe(clientToken);
    Queue<String> q = queues.get(clientToken);
    if (q != null && !q.isEmpty()) {
      String[] msgs = q.toArray(new String[0]);
      q.clear();
      result.setResult(ResponseEntity.ok(msgs));
      return result;
    }
    pending.put(clientToken, result);
    result.onTimeout(
        () -> {
          pending.remove(clientToken, result);
          result.setResult(ResponseEntity.status(HttpStatus.NO_CONTENT).build());
        });
    result.onCompletion(() -> pending.remove(clientToken, result));
    return result;
  }

  /** Fan-out JSON to every subscribed browser client token. */
  public void broadcast(String json) {
    if (json == null || json.isBlank()) {
      return;
    }
    // Snapshot to avoid concurrent modification surprises
    Set<String> tokens = new CopyOnWriteArraySet<>(subscribers);
    for (String token : tokens) {
      DeferredResult<ResponseEntity<?>> dr = pending.remove(token);
      if (dr != null && !dr.isSetOrExpired()) {
        dr.setResult(ResponseEntity.ok(new String[] {json}));
        continue;
      }
      Queue<String> q = queues.computeIfAbsent(token, k -> new ConcurrentLinkedQueue<>());
      if (q.size() >= MAX_QUEUE) {
        q.poll();
        logger.warn("Workspace long-poll queue full for a client; dropping oldest");
      }
      q.offer(json);
    }
  }

  public int subscriberCount() {
    return subscribers.size();
  }

  /** Test helper. */
  public void clearForTests() {
    subscribers.clear();
    queues.clear();
    pending.clear();
  }
}
