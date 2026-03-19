package com.acme.seguradora.infrastructure.web.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoverageRequest {
    @NotBlank
    private String name;
    @NotNull
    @Positive
    private BigDecimal value;
}
