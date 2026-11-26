package com.gw.api.v1.workspace;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/** Ephemeral soft presence: recent agent API activity per object (not a lock). */
@Component
public class AgentPresenceTracker {

  public static final long DEFAULT_TTL_MS = 5 * 60 * 1000L;

  public record Presence(String actor, String tokenFingerprint, Instant lastSeen) {}

  private final ConcurrentHashMap<String, Presence> byKey = new ConcurrentHashMap<>();
  private final long ttlMs;

  public AgentPresenceTracker() {
    this(DEFAULT_TTL_MS);
  }

  public AgentPresenceTracker(long ttlMs) {
    this.ttlMs = ttlMs;
  }

  public void touch(String objectKind, String objectId, String fingerprint) {
    touch(objectKind, objectId, fingerprint, "agent");
  }

  /** Records that {@code actor} ("agent" or "human") is working on the object. */
  public void touch(String objectKind, String objectId, String fingerprint, String actor) {
    if (objectKind == null || objectId == null) {
      return;
    }
    String key = objectKind + ":" + objectId;
    byKey.put(key, new Presence(actor == null ? "agent" : actor, fingerprint, Instant.now()));
  }

  public Presence getIfLive(String objectKind, String objectId) {
    if (objectKind == null || objectId == null) {
      return null;
    }
    String key = objectKind + ":" + objectId;
    Presence p = byKey.get(key);
    if (p == null) {
      return null;
    }
    if (Instant.now().toEpochMilli() - p.lastSeen().toEpochMilli() > ttlMs) {
      byKey.remove(key, p);
      return null;
    }
    return p;
  }

  public Map<String, Presence> snapshotLive() {
    Instant now = Instant.now();
    ConcurrentHashMap<String, Presence> live = new ConcurrentHashMap<>();
    for (Map.Entry<String, Presence> e : byKey.entrySet()) {
      Presence p = e.getValue();
      if (now.toEpochMilli() - p.lastSeen().toEpochMilli() <= ttlMs) {
        live.put(e.getKey(), p);
      } else {
        byKey.remove(e.getKey(), p);
      }
    }
    return live;
  }
}
