package com.gw.api.v1;

/**
 * Request-scoped flags for agent API execution. Used so localhost runs can skip the GUI password
 * check when {@code geoweaver.agent.allow-localhost-runs=true}.
 */
public final class AgentApiAuthContext {

  private static final ThreadLocal<Boolean> SKIP_LOCALHOST_PASSWORD = new ThreadLocal<>();
  private static final ThreadLocal<String> TOKEN_FINGERPRINT = new ThreadLocal<>();

  private AgentApiAuthContext() {}

  public static void setSkipLocalhostPassword(boolean skip) {
    SKIP_LOCALHOST_PASSWORD.set(skip);
  }

  public static boolean isSkipLocalhostPassword() {
    return Boolean.TRUE.equals(SKIP_LOCALHOST_PASSWORD.get());
  }

  public static void setTokenFingerprint(String fp) {
    TOKEN_FINGERPRINT.set(fp);
  }

  public static String getTokenFingerprint() {
    return TOKEN_FINGERPRINT.get();
  }

  public static void clear() {
    SKIP_LOCALHOST_PASSWORD.remove();
    TOKEN_FINGERPRINT.remove();
  }
}
