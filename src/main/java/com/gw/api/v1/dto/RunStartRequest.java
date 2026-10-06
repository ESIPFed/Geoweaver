package com.gw.api.v1.dto;

public record RunStartRequest(String hostId, String hostPassword, String envId, String clientRunKey) {}
