package com.gw.api.v1.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gw.api.v1.AgentApiProperties;
import com.gw.api.v1.dto.TokenMetadata;
import com.gw.database.AgentApiTokenRepository;
import com.gw.jpa.AgentApiToken;
import com.gw.utils.RandomString;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/**
 * Issues and validates Agent API Bearer tokens. Raw secrets are shown once at mint; the database
 * stores only a SHA-256 hash plus expiry and revoke metadata.
 */
@Service
public class AgentApiTokenService {

  private static final Logger logger = LoggerFactory.getLogger(AgentApiTokenService.class);
  private static final int MIN_TOKEN_LENGTH = 32;
  public static final int HARD_MAX_TTL_DAYS = 180;

  private final AgentApiProperties properties;
  private final AgentApiTokenRepository repository;
  private final SecureRandom secureRandom = new SecureRandom();
  private final ObjectMapper objectMapper = new ObjectMapper();
  private final AtomicBoolean migrated = new AtomicBoolean(false);

  public AgentApiTokenService(AgentApiProperties properties, AgentApiTokenRepository repository) {
    this.properties = properties;
    this.repository = repository;
  }

  /** Leftover CHG-0001 plaintext file path (import-then-delete only; never used for auth). */
  public Path legacyTokenFilePath() {
    String configured = properties.getApiTokenFile();
    if (configured == null || configured.isBlank()) {
      return Path.of(System.getProperty("user.home"), "gw-workspace", ".agent_api_token")
          .toAbsolutePath()
          .normalize();
    }
    return Path.of(configured).toAbsolutePath().normalize();
  }

  public boolean hasActiveToken() {
    migrateLegacyFileOnce();
    return repository.countByRevokedAtIsNullAndExpiresAtAfter(Instant.now()) > 0;
  }

  public int resolveTtlDays(Integer requestedTtlDays) {
    int configuredMax = Math.min(HARD_MAX_TTL_DAYS, Math.max(1, properties.getTokenMaxTtlDays()));
    int configuredDefault = Math.min(configuredMax, Math.max(1, properties.getTokenTtlDays()));
    int ttl = requestedTtlDays == null ? configuredDefault : requestedTtlDays;
    if (ttl < 1) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ttlDays must be at least 1");
    }
    if (ttl > configuredMax) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "ttlDays cannot exceed " + configuredMax + " (maximum six months / 180 days)");
    }
    return ttl;
  }

  public boolean matches(String presented) {
    migrateLegacyFileOnce();
    try {
      if (presented == null || presented.length() < MIN_TOKEN_LENGTH) {
        return false;
      }
      Optional<AgentApiToken> found = repository.findByTokenHash(sha256Hex(presented));
      if (found.isEmpty()) {
        return false;
      }
      AgentApiToken row = found.get();
      if (row.getRevokedAt() != null) {
        return false;
      }
      if (Instant.now().isAfter(row.getExpiresAt())) {
        logger.warn(
            "Agent API token expired at {} (fingerprint={})",
            row.getExpiresAt(),
            row.getFingerprint());
        return false;
      }
      return true;
    } catch (Exception e) {
      logger.error("Failed to validate agent API token: {}", e.getMessage());
      return false;
    }
  }

  public String fingerprint(String token) {
    if (token == null) {
      return "none";
    }
    String hex = sha256Hex(token);
    return hex.substring(0, 12);
  }

  public IssuedToken createOrRotate() {
    return issue(null, false);
  }

  public IssuedToken createOrRotate(Integer requestedTtlDays) {
    return issue(requestedTtlDays, false);
  }

  /** Insert a new hashed token. Optionally revoke every other active token (explicit rotate). */
  @Transactional
  public IssuedToken issue(Integer requestedTtlDays, boolean revokeOthers) {
    migrateLegacyFileOnce();
    int ttlDays = resolveTtlDays(requestedTtlDays);
    Instant now = Instant.now();
    Instant expiresAt = now.plus(Duration.ofDays(ttlDays));
    byte[] bytes = new byte[32];
    secureRandom.nextBytes(bytes);
    String token = "gwt_" + HexFormat.of().formatHex(bytes);
    String hash = sha256Hex(token);
    String fingerprint = hash.substring(0, 12);

    if (revokeOthers) {
      revokeAllActive(now);
    }

    AgentApiToken row = new AgentApiToken();
    row.setId(RandomString.get(16));
    row.setTokenHash(hash);
    row.setFingerprint(fingerprint);
    row.setExpiresAt(expiresAt);
    row.setCreatedAt(now);
    row.setTtlDays(ttlDays);
    repository.save(row);

    logger.info(
        "Agent API token stored in database (hash only) fingerprint={} expiresAt={} ttlDays={} revokeOthers={}",
        fingerprint,
        expiresAt,
        ttlDays,
        revokeOthers);
    return new IssuedToken(token, expiresAt, ttlDays, fingerprint);
  }

  @Transactional
  public boolean revokeByFingerprint(String fingerprint) {
    migrateLegacyFileOnce();
    if (fingerprint == null || fingerprint.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "fingerprint is required");
    }
    Optional<AgentApiToken> found = repository.findByFingerprint(fingerprint.trim());
    if (found.isEmpty()) {
      return false;
    }
    AgentApiToken row = found.get();
    if (row.getRevokedAt() == null) {
      row.setRevokedAt(Instant.now());
      repository.save(row);
    }
    logger.info("Agent API token revoked fingerprint={}", row.getFingerprint());
    return true;
  }

  public List<TokenMetadata> listMetadata() {
    migrateLegacyFileOnce();
    List<TokenMetadata> out = new ArrayList<>();
    repository
        .findAll()
        .forEach(
            row ->
                out.add(
                    new TokenMetadata(
                        row.getFingerprint(),
                        row.getExpiresAt().toString(),
                        row.getCreatedAt().toString(),
                        row.getRevokedAt() == null ? null : row.getRevokedAt().toString(),
                        row.getTtlDays() == null ? 0 : row.getTtlDays())));
    return out;
  }

  void migrateLegacyFileOnce() {
    if (!migrated.compareAndSet(false, true)) {
      return;
    }
    migrateLegacyFile();
  }

  private void migrateLegacyFile() {
    Path path = legacyTokenFilePath();
    if (!Files.isRegularFile(path)) {
      return;
    }
    boolean safeToDelete = false;
    try {
      String raw = Files.readString(path, StandardCharsets.UTF_8).trim();
      if (raw.startsWith("{")) {
        JsonNode n = objectMapper.readTree(raw);
        String token = textOrNull(n, "token");
        String exp = textOrNull(n, "expiresAt");
        if (token == null || token.length() < MIN_TOKEN_LENGTH || exp == null) {
          logger.warn(
              "Leftover Agent API token file at {} is incomplete; deleting (auth never uses files).",
              path);
          safeToDelete = true;
        } else {
          Instant expiresAt = Instant.parse(exp);
          if (Instant.now().isAfter(expiresAt)) {
            logger.warn(
                "Leftover Agent API token file at {} is expired; deleting without import.", path);
            safeToDelete = true;
          } else if (repository.findByTokenHash(sha256Hex(token)).isPresent()) {
            // Already in DB from a prior import attempt.
            safeToDelete = true;
          } else {
            AgentApiToken row = new AgentApiToken();
            row.setId(RandomString.get(16));
            String hash = sha256Hex(token);
            row.setTokenHash(hash);
            row.setFingerprint(hash.substring(0, 12));
            row.setExpiresAt(expiresAt);
            row.setCreatedAt(Instant.now());
            Integer ttl =
                n.has("ttlDays") && n.get("ttlDays").canConvertToInt()
                    ? n.get("ttlDays").asInt()
                    : resolveTtlDays(null);
            row.setTtlDays(ttl);
            repository.save(row);
            logger.warn(
                "Imported leftover Agent API token file into the database (hash only) fingerprint={}",
                row.getFingerprint());
            safeToDelete = true;
          }
        }
      } else {
        logger.warn(
            "Ignoring leftover Agent API token file at {} (no expiresAt JSON). Deleting; remint required.",
            path);
        safeToDelete = true;
      }
    } catch (Exception e) {
      // Do not delete on failure — operator may still recover the secret from the file once.
      logger.error(
          "Could not import leftover Agent API token file at {} — leaving file in place; auth still ignores it: {}",
          path,
          e.getMessage());
      return;
    }
    if (!safeToDelete) {
      return;
    }
    try {
      Files.deleteIfExists(path);
      logger.info("Deleted leftover plaintext Agent API token file at {}", path);
    } catch (Exception e) {
      logger.error(
          "Failed to delete leftover plaintext Agent API token file at {} — auth will still ignore it: {}",
          path,
          e.getMessage());
    }
  }

  private void revokeAllActive(Instant now) {
    for (AgentApiToken row : repository.findByRevokedAtIsNull()) {
      row.setRevokedAt(now);
      repository.save(row);
    }
  }

  static String sha256Hex(String token) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      return HexFormat.of().formatHex(md.digest(token.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }

  private static String textOrNull(JsonNode n, String field) {
    JsonNode v = n.get(field);
    if (v == null || v.isNull() || !v.isTextual()) {
      return null;
    }
    String s = v.asText().trim();
    return s.isEmpty() ? null : s;
  }

  public record IssuedToken(String token, Instant expiresAt, int ttlDays, String fingerprint) {}
}
