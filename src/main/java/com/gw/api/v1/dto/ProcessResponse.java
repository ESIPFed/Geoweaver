package com.gw.api.v1.dto;

public record ProcessResponse(
    String id, String name, String lang, String description, String code, String owner) {}
