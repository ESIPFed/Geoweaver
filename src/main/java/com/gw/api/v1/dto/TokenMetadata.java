package com.gw.api.v1.dto;

/** Public metadata for a managed Agent API token. Never includes the raw secret. */
public record TokenMetadata(
    String fingerprint, String expiresAt, String createdAt, String revokedAt, int ttlDays) {}
