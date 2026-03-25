package com.acme.seguradora.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.util.List;

public record CreateQuoteRequest(

        @NotBlank
        @JsonProperty("product_id")
        String productId,

        @NotBlank
        @JsonProperty("offer_id")
        String offerId,

        @NotBlank
        String category,

        @NotNull
        @Positive
        @JsonProperty("total_monthly_premium_amount")
        BigDecimal totalMonthlyPremiumAmount,

        @NotNull
        @Positive
        @JsonProperty("total_coverage_amount")
        BigDecimal totalCoverageAmount,

        @NotEmpty
        @Valid
        List<CoverageRequest> coverages,

        List<String> assistances,

        @NotNull
        @Valid
        CustomerRequest customer) {}
