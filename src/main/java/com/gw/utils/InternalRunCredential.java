package com.gw.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Random per-JVM secret that in-process callers (Agent API, CLI) pass as the localhost password
 * after they have authorized the caller themselves. It is never logged, returned, or persisted, so
 * an HTTP client can only run on localhost with the real GUI password or through the Agent API.
 */
public final class InternalRunCredential {

  private static final String SECRET;

  static {
    byte[] bytes = new byte[32];
    new SecureRandom().nextBytes(bytes);
    SECRET = "gwint_" + Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private InternalRunCredential() {}

  public static String get() {
    return SECRET;
  }

  public static boolean matches(String password) {
    if (password == null || password.isEmpty()) {
      return false;
    }
    return MessageDigest.isEqual(
        SECRET.getBytes(StandardCharsets.UTF_8), password.getBytes(StandardCharsets.UTF_8));
  }
}
