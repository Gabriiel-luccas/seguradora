package com.acme.seguradora.infrastructure.web.dto.response;

import com.acme.seguradora.domain.model.QuoteStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record QuoteResponse(
        Long id,
        @JsonProperty("insurance_policy_id") Long policyId,
        @JsonProperty("product_id") String productId,
        @JsonProperty("offer_id") String offerId,
        String category,
        @JsonProperty("created_at") LocalDateTime createdAt,
        @JsonProperty("updated_at") LocalDateTime updatedAt,
        @JsonProperty("total_monthly_premium_amount") BigDecimal totalMonthlyPremiumAmount,
        @JsonProperty("total_coverage_amount") BigDecimal totalCoverageAmount,
        List<CoverageResponse> coverages,
        List<String> assistances,
        CustomerResponse customer,
        QuoteStatus status) {}
