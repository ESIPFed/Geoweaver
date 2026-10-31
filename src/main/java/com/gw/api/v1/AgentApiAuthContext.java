package com.gw.api.v1;

/** Request-scoped identity of the Agent API caller (token fingerprint for audit logs). */
public final class AgentApiAuthContext {

  private static final ThreadLocal<String> TOKEN_FINGERPRINT = new ThreadLocal<>();

  private AgentApiAuthContext() {}

  public static void setTokenFingerprint(String fp) {
    TOKEN_FINGERPRINT.set(fp);
  }

  public static String getTokenFingerprint() {
    return TOKEN_FINGERPRINT.get();
  }

  public static void clear() {
    TOKEN_FINGERPRINT.remove();
  }
}
