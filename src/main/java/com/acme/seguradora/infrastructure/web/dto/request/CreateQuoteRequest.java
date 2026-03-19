package com.acme.seguradora.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CreateQuoteRequest {

    @NotBlank
    @JsonProperty("product_id")
    private String productId;

    @NotBlank
    @JsonProperty("offer_id")
    private String offerId;

    @NotBlank
    private String category;

    @NotNull
    @Positive
    @JsonProperty("total_monthly_premium_amount")
    private BigDecimal totalMonthlyPremiumAmount;

    @NotNull
    @Positive
    @JsonProperty("total_coverage_amount")
    private BigDecimal totalCoverageAmount;

    @NotEmpty
    @Valid
    private List<CoverageRequest> coverages;

    private List<String> assistances;

    @NotNull
    @Valid
    private CustomerRequest customer;
}
