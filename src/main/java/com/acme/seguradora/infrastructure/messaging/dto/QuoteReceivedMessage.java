package com.acme.seguradora.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record QuoteReceivedMessage(
        @JsonProperty("quote_id") Long quoteId,
        @JsonProperty("product_id") String productId,
        @JsonProperty("offer_id") String offerId,
        String category,
        @JsonProperty("total_monthly_premium_amount") BigDecimal totalMonthlyPremiumAmount,
        @JsonProperty("total_coverage_amount") BigDecimal totalCoverageAmount,
        Map<String, BigDecimal> coverages,
        List<String> assistances,
        @JsonProperty("customer_document_number") String customerDocumentNumber,
        @JsonProperty("customer_name") String customerName,
        @JsonProperty("created_at") LocalDateTime createdAt) {}
