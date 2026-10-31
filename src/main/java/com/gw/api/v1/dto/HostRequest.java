package com.gw.api.v1.dto;

/**
 * Create or update a host. {@code type} is {@code ssh} (default), {@code jupyter}, {@code
 * jupyterhub}, {@code jupyterlab} or {@code gee}. ssh hosts need {@code ip} and {@code username}
 * ({@code port} defaults to 22); the others need {@code url}. No password field: SSH passwords are
 * sent per run and never stored.
 */
public record HostRequest(
    String name, String type, String ip, String port, String username, String url) {}
