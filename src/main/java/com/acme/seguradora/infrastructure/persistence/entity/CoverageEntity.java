package com.acme.seguradora.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "quote_coverages")
public class CoverageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coverage_seq")
    @SequenceGenerator(name = "coverage_seq", sequenceName = "coverage_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private QuoteEntity quote;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "value", nullable = false, precision = 15, scale = 2)
    private BigDecimal value;

    protected CoverageEntity() {}

    public CoverageEntity(String name, BigDecimal value) {
        this.name = name;
        this.value = value;
    }

    public Long getId() { return id; }
    public QuoteEntity getQuote() { return quote; }
    public String getName() { return name; }
    public BigDecimal getValue() { return value; }

    public void setQuote(QuoteEntity quote) { this.quote = quote; }
}

