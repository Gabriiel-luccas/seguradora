package com.acme.seguradora.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CoverageRequest(

        @NotBlank
        String name,

        @NotNull
        @Positive
        BigDecimal value) {}
