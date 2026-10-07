package com.gw.api.v1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

/**
 * HTTP mint of an Agent API token against a <strong>running</strong> Geoweaver (any reachable
 * host). Used when the CLI caller is not the same machine as the server, or when the operator
 * wants the token stored in the server process database rather than this CLI JVM.
 */
public final class AgentTokenRemoteMint {

  private static final ObjectMapper MAPPER = new ObjectMapper();

  public record Result(
      String token, String fingerprint, String expiresAt, int ttlDays, int httpStatus) {}

  private AgentTokenRemoteMint() {}

  /** Strip trailing slashes. {@code baseUrl} must include the context path (usually {@code /Geoweaver}). */
  public static String normalizeBaseUrl(String baseUrl) {
    if (baseUrl == null || baseUrl.isBlank()) {
      throw new IllegalArgumentException("base URL is required (example: http://host:8070/Geoweaver)");
    }
    String u = baseUrl.trim();
    while (u.endsWith("/")) {
      u = u.substring(0, u.length() - 1);
    }
    return u;
  }

  public static Result createToken(
      String baseUrl, String hostPassword, Integer ttlDays, boolean revokeOthers) throws IOException {
    if (hostPassword == null || hostPassword.isBlank()) {
      throw new IllegalArgumentException(
          "hostPassword is required (Geoweaver GUI localhost password on the server)");
    }
    String url = normalizeBaseUrl(baseUrl) + "/api/v1/tokens";
    StringBuilder json = new StringBuilder("{");
    json.append("\"hostPassword\":").append(AgentTokenBrowserReveal.jsonString(hostPassword));
    if (ttlDays != null) {
      json.append(",\"ttlDays\":").append(ttlDays);
    }
    if (revokeOthers) {
      json.append(",\"revokeOthers\":true");
    }
    json.append("}");
    HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
    conn.setConnectTimeout(10_000);
    conn.setReadTimeout(30_000);
    conn.setRequestMethod("POST");
    conn.setDoOutput(true);
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Accept", "application/json");
    byte[] payload = json.toString().getBytes(StandardCharsets.UTF_8);
    conn.setFixedLengthStreamingMode(payload.length);
    try (OutputStream os = conn.getOutputStream()) {
      os.write(payload);
    }
    int code = conn.getResponseCode();
    InputStream in = code >= 200 && code < 300 ? conn.getInputStream() : conn.getErrorStream();
    String body = in == null ? "" : new String(in.readAllBytes(), StandardCharsets.UTF_8);
    conn.disconnect();
    if (code < 200 || code >= 300) {
      throw new IOException("POST " + url + " failed HTTP " + code + " " + safeError(body));
    }
    JsonNode n = MAPPER.readTree(body);
    JsonNode tokenNode = n.get("token");
    JsonNode fpNode = n.get("fingerprint");
    if (tokenNode == null || tokenNode.asText().isBlank() || fpNode == null) {
      throw new IOException("POST " + url + " succeeded but response had no token/fingerprint");
    }
    int ttl = n.has("ttlDays") ? n.get("ttlDays").asInt() : 0;
    String expires = n.has("expiresAt") ? n.get("expiresAt").asText() : "";
    return new Result(tokenNode.asText(), fpNode.asText(), expires, ttl, code);
  }

  static String safeError(String body) {
    if (body == null || body.isBlank()) {
      return "";
    }
    // Do not echo secrets if a misbehaving server returned one.
    String trimmed = body.length() > 500 ? body.substring(0, 500) : body;
    return trimmed.replaceAll("gwt_[A-Za-z0-9]+", "gwt_[redacted]");
  }
}
