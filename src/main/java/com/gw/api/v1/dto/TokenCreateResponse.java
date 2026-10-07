package com.gw.api.v1.dto;

/**
 * One-time delivery of a new Agent API token. The raw {@code token} is not stored; only a hash is
 * kept in the Geoweaver database.
 */
public record TokenCreateResponse(
    String token,
    String fingerprint,
    String expiresAt,
    int ttlDays,
    String authorizationHeaderHint,
    boolean previousTokenInvalidated) {}
