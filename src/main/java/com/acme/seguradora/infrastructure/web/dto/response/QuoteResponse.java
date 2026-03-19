package com.acme.seguradora.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.acme.seguradora.domain.model.QuoteStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteResponse {
    private Long id;
    @JsonProperty("product_id")
    private String productId;
    @JsonProperty("offer_id")
    private String offerId;
    private String category;
    @JsonProperty("total_monthly_premium_amount")
    private BigDecimal totalMonthlyPremiumAmount;
    @JsonProperty("total_coverage_amount")
    private BigDecimal totalCoverageAmount;
    private List<CoverageResponse> coverages;
    private List<String> assistances;
    private CustomerResponse customer;
    @JsonProperty("policy_id")
    private Long policyId;
    private QuoteStatus status;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
