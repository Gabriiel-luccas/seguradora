package com.acme.seguradora.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CatalogOfferDto {

    private String id;
    private String productId;
    private String name;
    private boolean active;
    private Map<String, BigDecimal> coverages;
    private List<String> assistances;
    private BigDecimal minMonthlyPremiumAmount;
    private BigDecimal maxMonthlyPremiumAmount;
    private BigDecimal maxCoverageAmount;
}
