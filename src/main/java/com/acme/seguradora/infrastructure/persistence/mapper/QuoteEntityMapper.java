package com.acme.seguradora.infrastructure.persistence.mapper;

import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.persistence.entity.QuoteEntity;
import org.springframework.stereotype.Component;

@Component
public class QuoteEntityMapper {

    public QuoteEntity toEntity(Quote quote) {
        return QuoteEntity.builder()
                .id(quote.id())
                .productId(quote.productId())
                .offerId(quote.offerId())
                .category(quote.category())
                .totalMonthlyPremiumAmount(quote.totalMonthlyPremiumAmount())
                .totalCoverageAmount(quote.totalCoverageAmount())
                .coverages(quote.coverages())
                .assistances(quote.assistances())
                .customer(quote.customer())
                .policyId(quote.policyId())
                .status(quote.status())
                .createdAt(quote.createdAt())
                .updatedAt(quote.updatedAt())
                .build();
    }

    public Quote toDomain(QuoteEntity entity) {
        return new Quote(
                entity.getId(),
                entity.getProductId(),
                entity.getOfferId(),
                entity.getCategory(),
                entity.getTotalMonthlyPremiumAmount(),
                entity.getTotalCoverageAmount(),
                entity.getCoverages(),
                entity.getAssistances(),
                entity.getCustomer(),
                entity.getPolicyId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}

