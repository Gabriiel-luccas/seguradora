package com.acme.seguradora.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record Quote(
        Long id,
        String productId,
        String offerId,
        String category,
        BigDecimal totalMonthlyPremiumAmount,
        BigDecimal totalCoverageAmount,
        List<Coverage> coverages,
        List<String> assistances,
        Customer customer,
        Long policyId,
        QuoteStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
