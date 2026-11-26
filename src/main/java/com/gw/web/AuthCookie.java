package com.gw.web;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Value of the "logged in" browser cookie: a signature only this server can make, so the cookie
 * cannot be written by hand.
 *
 * <p>Format {@code v1.<expiry epoch seconds>.<HMAC-SHA256>}. The signing key is a random file in
 * the Geoweaver folder, mixed with the current localhost password hash, so resetting the password
 * signs everyone out.
 */
public final class AuthCookie {

  static final String KEY_FILE = ".auth_cookie_key";
  static final long LIFETIME_SECONDS = 30L * 24 * 60 * 60;

  private static final SecureRandom RANDOM = new SecureRandom();

  private AuthCookie() {}

  /** A fresh cookie value, valid for 30 days. */
  public static String issue() {
    long expiry = Instant.now().getEpochSecond() + LIFETIME_SECONDS;
    return "v1." + expiry + "." + sign(expiry);
  }

  /** True only for an unexpired value this server signed with the current password. */
  public static boolean isValid(String value) {
    if (value == null) return false;
    String[] parts = value.split("\\.");
    if (parts.length != 3 || !"v1".equals(parts[0])) return false;
    long expiry;
    try {
      expiry = Long.parseLong(parts[1]);
    } catch (NumberFormatException e) {
      return false;
    }
    if (expiry < Instant.now().getEpochSecond()) return false;
    String expected = sign(expiry);
    return expected != null
        && MessageDigest.isEqual(
            expected.getBytes(StandardCharsets.US_ASCII), parts[2].getBytes(StandardCharsets.US_ASCII));
  }

  private static String sign(long expiry) {
    try {
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(signingKey(), "HmacSHA256"));
      byte[] sig = mac.doFinal(("gw_localhost_auth|" + expiry).getBytes(StandardCharsets.UTF_8));
      return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
    } catch (Exception e) {
      return null;
    }
  }

  private static Path folder() {
    return Paths.get(System.getProperty("user.home"), "geoweaver");
  }

  private static byte[] signingKey() throws Exception {
    MessageDigest d = MessageDigest.getInstance("SHA-256");
    d.update(randomKey());
    Path secret = folder().resolve(".secret");
    if (Files.isRegularFile(secret)) {
      d.update(Files.readAllBytes(secret));
    }
    return d.digest();
  }

  private static synchronized byte[] randomKey() throws IOException {
    Path file = folder().resolve(KEY_FILE);
    if (Files.isRegularFile(file)) {
      byte[] k = Files.readAllBytes(file);
      if (k.length >= 32) return k;
    }
    Files.createDirectories(folder());
    byte[] k = new byte[32];
    RANDOM.nextBytes(k);
    Path tmp = Files.createTempFile(folder(), KEY_FILE, ".tmp");
    try {
      try {
        Files.setPosixFilePermissions(tmp, PosixFilePermissions.fromString("rw-------"));
      } catch (UnsupportedOperationException ignored) {
        // Windows: the user's profile folder is already private
      }
      Files.write(tmp, k);
      Files.move(tmp, file, java.nio.file.StandardCopyOption.ATOMIC_MOVE);
    } finally {
      Files.deleteIfExists(tmp);
    }
    return Files.readAllBytes(file);
  }
}
