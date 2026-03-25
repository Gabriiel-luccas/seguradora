package com.acme.seguradora.infrastructure.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record MonthlyPremiumAmountResponse (
        @JsonProperty("max_amount") BigDecimal maxAmount,
        @JsonProperty("min_amount") BigDecimal minAmount,
        @JsonProperty("suggested_amount") BigDecimal suggestedAmount) {
}
