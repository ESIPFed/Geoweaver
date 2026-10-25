package com.gw.api.v1.dto;

/** A Python environment found on a host; pass {@code id} as {@code envId} when starting a run. */
public record EnvironmentResponse(
    String id, String name, String type, String bin, String pyenv, String basedir, String hostId) {}
