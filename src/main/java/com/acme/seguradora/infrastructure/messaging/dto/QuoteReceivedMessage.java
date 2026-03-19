package com.acme.seguradora.infrastructure.messaging.dto;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
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
public class QuoteReceivedMessage {
    private Long quoteId;
    private String productId;
    private String offerId;
    private String category;
    private BigDecimal totalMonthlyPremiumAmount;
    private BigDecimal totalCoverageAmount;
    private List<Coverage> coverages;
    private List<String> assistances;
    private Customer customer;
    private LocalDateTime createdAt;
}
