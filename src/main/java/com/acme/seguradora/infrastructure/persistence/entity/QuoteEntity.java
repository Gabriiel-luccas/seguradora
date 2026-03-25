package com.acme.seguradora.infrastructure.persistence.entity;

import com.acme.seguradora.domain.model.QuoteStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CoverageEntity> coverages = new ArrayList<>();

    @OneToMany(mappedBy = "quote", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssistanceEntity> assistances = new ArrayList<>();

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private CustomerEntity customer;

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
    private LocalDateTime updatedAt;

    protected QuoteEntity() {}

    private QuoteEntity(Builder builder) {
        this.id = builder.id;
        this.productId = builder.productId;
        this.offerId = builder.offerId;
        this.category = builder.category;
        this.totalMonthlyPremiumAmount = builder.totalMonthlyPremiumAmount;
        this.totalCoverageAmount = builder.totalCoverageAmount;
        this.coverages = builder.coverages != null ? builder.coverages : new ArrayList<>();
        this.assistances = builder.assistances != null ? builder.assistances : new ArrayList<>();
        this.customer = builder.customer;
        this.policyId = builder.policyId;
        this.status = builder.status;
        this.createdAt = builder.createdAt;
        this.updatedAt = builder.updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getOfferId() { return offerId; }
    public String getCategory() { return category; }
    public BigDecimal getTotalMonthlyPremiumAmount() { return totalMonthlyPremiumAmount; }
    public BigDecimal getTotalCoverageAmount() { return totalCoverageAmount; }
    public List<CoverageEntity> getCoverages() { return coverages; }
    public List<AssistanceEntity> getAssistances() { return assistances; }
    public CustomerEntity getCustomer() { return customer; }
    public Long getPolicyId() { return policyId; }
    public QuoteStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setId(Long id) { this.id = id; }
    public void setProductId(String productId) { this.productId = productId; }
    public void setOfferId(String offerId) { this.offerId = offerId; }
    public void setCategory(String category) { this.category = category; }
    public void setTotalMonthlyPremiumAmount(BigDecimal v) { this.totalMonthlyPremiumAmount = v; }
    public void setTotalCoverageAmount(BigDecimal v) { this.totalCoverageAmount = v; }
    public void setCoverages(List<CoverageEntity> coverages) { this.coverages = coverages; }
    public void setAssistances(List<AssistanceEntity> assistances) { this.assistances = assistances; }
    public void setCustomer(CustomerEntity customer) { this.customer = customer; }
    public void setPolicyId(Long policyId) { this.policyId = policyId; }
    public void setStatus(QuoteStatus status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public static final class Builder {
        private Long id;
        private String productId;
        private String offerId;
        private String category;
        private BigDecimal totalMonthlyPremiumAmount;
        private BigDecimal totalCoverageAmount;
        private List<CoverageEntity> coverages;
        private List<AssistanceEntity> assistances;
        private CustomerEntity customer;
        private Long policyId;
        private QuoteStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        private Builder() {}

        public Builder id(Long id) { this.id = id; return this; }
        public Builder productId(String v) { this.productId = v; return this; }
        public Builder offerId(String v) { this.offerId = v; return this; }
        public Builder category(String v) { this.category = v; return this; }
        public Builder totalMonthlyPremiumAmount(BigDecimal v) { this.totalMonthlyPremiumAmount = v; return this; }
        public Builder totalCoverageAmount(BigDecimal v) { this.totalCoverageAmount = v; return this; }
        public Builder coverages(List<CoverageEntity> v) { this.coverages = v; return this; }
        public Builder assistances(List<AssistanceEntity> v) { this.assistances = v; return this; }
        public Builder customer(CustomerEntity v) { this.customer = v; return this; }
        public Builder policyId(Long v) { this.policyId = v; return this; }
        public Builder status(QuoteStatus v) { this.status = v; return this; }
        public Builder createdAt(LocalDateTime v) { this.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v) { this.updatedAt = v; return this; }
        public QuoteEntity build() { return new QuoteEntity(this); }
    }
}
