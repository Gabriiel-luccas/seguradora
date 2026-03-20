package com.acme.seguradora.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteReceivedMessage {

    @JsonProperty("quote_id")
    private Long quoteId;

    @JsonProperty("product_id")
    private String productId;

    @JsonProperty("offer_id")
    private String offerId;

    private String category;

    @JsonProperty("total_monthly_premium_amount")
    private BigDecimal totalMonthlyPremiumAmount;

    @JsonProperty("total_coverage_amount")
    private BigDecimal totalCoverageAmount;

    private Map<String, BigDecimal> coverages;

    private List<String> assistances;

    @JsonProperty("customer_document_number")
    private String customerDocumentNumber;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
