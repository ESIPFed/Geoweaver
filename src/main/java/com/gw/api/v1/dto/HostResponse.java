package com.gw.api.v1.dto;

/**
 * A Geoweaver host. {@code local} is true for the Geoweaver server itself. Geoweaver never stores
 * host passwords; send {@code hostPassword} with each run instead.
 */
public record HostResponse(
    String id,
    String name,
    String type,
    String ip,
    String port,
    String username,
    String url,
    boolean local) {}
