package com.acme.seguradora.infrastructure.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OfferApiResponse {
    private String id;
    @JsonProperty("product_id")
    private String productId;
    private String name;
    private boolean active;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    @JsonProperty("monthly_premium")
    private MonthlyPremiumResponse monthlyPremium;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MonthlyPremiumResponse {
        @JsonProperty("min_amount")
        private BigDecimal minAmount;
        @JsonProperty("max_amount")
        private BigDecimal maxAmount;
        @JsonProperty("suggested_amount")
        private BigDecimal suggestedAmount;
    }
}
