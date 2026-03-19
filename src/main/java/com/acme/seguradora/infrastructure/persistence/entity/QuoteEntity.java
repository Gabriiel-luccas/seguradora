package com.acme.seguradora.infrastructure.persistence.entity;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.QuoteStatus;
import com.acme.seguradora.infrastructure.persistence.converter.CoverageListConverter;
import com.acme.seguradora.infrastructure.persistence.converter.CustomerConverter;
import com.acme.seguradora.infrastructure.persistence.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "quotes")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quote_seq")
    @SequenceGenerator(name = "quote_seq", sequenceName = "quote_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "offer_id", nullable = false)
    private String offerId;

    @Column(nullable = false)
    private String category;

    @Column(name = "total_monthly_premium_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(name = "total_coverage_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalCoverageAmount;

    @Convert(converter = CoverageListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<Coverage> coverages;

    @Convert(converter = StringListConverter.class)
    @Column(columnDefinition = "TEXT")
    private List<String> assistances;

    @Convert(converter = CustomerConverter.class)
    @Column(columnDefinition = "TEXT")
    private Customer customer;

    @Column(name = "policy_id")
    private Long policyId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuoteStatus status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
