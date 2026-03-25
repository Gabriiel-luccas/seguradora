package com.acme.seguradora.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String error,
        String message,
        LocalDateTime timestamp,
        @JsonProperty("validation_errors") List<String> validationErrors) {}
