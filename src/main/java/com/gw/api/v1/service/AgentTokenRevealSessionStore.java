package com.gw.api.v1.service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Service;

/** Short-lived one-time token reveal sessions for the Geoweaver browser copy page (port 8070). */
@Service
public class AgentTokenRevealSessionStore {

  private static final long TTL_SECONDS = 300;

  public record Session(String token, String fingerprint, String expiresAt, Instant createdAt) {}

  private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, Boolean> acked = new ConcurrentHashMap<>();

  public String create(String token, String fingerprint, String expiresAt) {
    purgeExpired();
    if (token == null || !token.startsWith("gwt_")) {
      throw new IllegalArgumentException("valid Agent API token required");
    }
    String nonce = randomNonce();
    sessions.put(
        nonce,
        new Session(
            token,
            fingerprint == null ? "(unknown)" : fingerprint,
            expiresAt == null ? "(see token file)" : expiresAt,
            Instant.now()));
    return nonce;
  }

  public Session get(String nonce) {
    if (nonce == null) {
      return null;
    }
    Session s = sessions.get(nonce);
    if (s == null) {
      return null;
    }
    if (s.createdAt().plusSeconds(TTL_SECONDS).isBefore(Instant.now())) {
      sessions.remove(nonce);
      return null;
    }
    return s;
  }

  public boolean acknowledge(String nonce) {
    Session s = sessions.remove(nonce);
    if (s == null) {
      return acked.containsKey(nonce);
    }
    acked.put(nonce, Boolean.TRUE);
    return true;
  }

  public boolean isAcked(String nonce) {
    return nonce != null && acked.containsKey(nonce);
  }

  private void purgeExpired() {
    Instant cutoff = Instant.now().minusSeconds(TTL_SECONDS);
    Iterator<Map.Entry<String, Session>> it = sessions.entrySet().iterator();
    while (it.hasNext()) {
      Map.Entry<String, Session> e = it.next();
      if (e.getValue().createdAt().isBefore(cutoff)) {
        it.remove();
      }
    }
  }

  private static String randomNonce() {
    byte[] buf = new byte[24];
    new SecureRandom().nextBytes(buf);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
  }
}
