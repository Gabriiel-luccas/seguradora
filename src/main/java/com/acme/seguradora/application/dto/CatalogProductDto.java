package com.acme.seguradora.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record CatalogProductDto(
        String id,
        String name,
        boolean active,
        List<String> offersIds,
        @JsonProperty("created_at")
        LocalDateTime createdAt) {}
