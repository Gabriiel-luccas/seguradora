package com.acme.seguradora.infrastructure.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ProductApiResponse(
        String id,
        String name,
        boolean active,

        @JsonProperty("offers_ids")
        List<String> offersIds,

        @JsonProperty("created_at")
        LocalDateTime createdAt) {}
