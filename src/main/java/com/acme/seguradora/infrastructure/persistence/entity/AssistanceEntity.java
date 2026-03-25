package com.acme.seguradora.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "quote_assistances")
public class AssistanceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "assistance_seq")
    @SequenceGenerator(name = "assistance_seq", sequenceName = "assistance_sequence", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quote_id", nullable = false)
    private QuoteEntity quote;

    @Column(name = "name", nullable = false)
    private String name;

    protected AssistanceEntity() {}

    public AssistanceEntity(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public QuoteEntity getQuote() { return quote; }
    public String getName() { return name; }

    public void setQuote(QuoteEntity quote) { this.quote = quote; }
}

