package com.acme.seguradora.infrastructure.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record OfferApiResponse(
        String id,

        @JsonProperty("product_id")
        String productId,

        String name,
        @JsonProperty("created_at")
        LocalDateTime createdAt,
        boolean active,
        Map<String, BigDecimal> coverages,
        List<String> assistances,
        @JsonProperty("monthly_premium_amount")
        MonthlyPremiumAmountResponse monthlyPremiumAmount
) {
}
