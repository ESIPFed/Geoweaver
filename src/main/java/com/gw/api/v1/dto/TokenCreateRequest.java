package com.gw.api.v1.dto;

/**
 * Bootstrap Agent API Bearer token using the GUI localhost password.
 *
 * @param hostPassword GUI localhost password (required)
 * @param ttlDays optional lifetime in days (1..180 / six months); omit for server default
 * @param revokeOthers if true, revoke every other active token (explicit rotate)
 */
public record TokenCreateRequest(String hostPassword, Integer ttlDays, Boolean revokeOthers) {}
