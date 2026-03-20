package com.acme.seguradora.infrastructure.persistence.entity;

import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.QuoteStatus;
import com.acme.seguradora.infrastructure.persistence.converter.CoverageListConverter;
import com.acme.seguradora.infrastructure.persistence.converter.CustomerConverter;
import com.acme.seguradora.infrastructure.persistence.converter.StringListConverter;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "quotes")
public class QuoteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "quote_seq")
    @SequenceGenerator(name = "quote_seq", sequenceName = "quote_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private String productId;

    @Column(name = "offer_id", nullable = false)
    private String offerId;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "total_monthly_premium_amount", precision = 15, scale = 2)
    private BigDecimal totalMonthlyPremiumAmount;

    @Column(name = "total_coverage_amount", precision = 15, scale = 2)
    private BigDecimal totalCoverageAmount;

    @Convert(converter = CoverageListConverter.class)
    @Column(name = "coverages", columnDefinition = "TEXT")
    private List<Coverage> coverages;

    @Convert(converter = StringListConverter.class)
    @Column(name = "assistances", columnDefinition = "TEXT")
    private List<String> assistances;

    @Convert(converter = CustomerConverter.class)
    @Column(name = "customer", columnDefinition = "TEXT")
    private Customer customer;

    @Column(name = "policy_id")
    private Long policyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50)
    private QuoteStatus status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;}
