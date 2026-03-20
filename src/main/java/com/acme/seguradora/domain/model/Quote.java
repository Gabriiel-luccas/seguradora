package com.acme.seguradora.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Quote {

    private Long id;
    private String productId;
    private String offerId;
    private String category;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal totalCoverageAmount;
    private List<Coverage> coverages;
    private List<String> assistances;
    private Customer customer;
    private Long policyId;
    private QuoteStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
