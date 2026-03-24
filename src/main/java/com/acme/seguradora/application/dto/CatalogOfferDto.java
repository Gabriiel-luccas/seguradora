package com.acme.seguradora.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record CatalogOfferDto(
        String id,
        String productId,
        String name,
        boolean active,
        Map<String, BigDecimal> coverages,
        List<String> assistances,
        BigDecimal minMonthlyPremiumAmount,
        BigDecimal maxMonthlyPremiumAmount,
        BigDecimal maxCoverageAmount) {}
