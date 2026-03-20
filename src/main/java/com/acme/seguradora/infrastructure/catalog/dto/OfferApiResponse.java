package com.acme.seguradora.infrastructure.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OfferApiResponse {

    private String id;

    @JsonProperty("product_id")
    private String productId;

    private String name;
    private boolean active;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;

    @JsonProperty("min_monthly_premium_amount")
    private BigDecimal minMonthlyPremiumAmount;

    @JsonProperty("max_monthly_premium_amount")
    private BigDecimal maxMonthlyPremiumAmount;

    @JsonProperty("max_coverage_amount")
    private BigDecimal maxCoverageAmount;
}
