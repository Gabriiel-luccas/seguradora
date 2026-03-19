package com.acme.seguradora.infrastructure.persistence.adapter;

import com.acme.seguradora.application.port.output.QuoteRepositoryPort;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.persistence.entity.QuoteEntity;
import com.acme.seguradora.infrastructure.persistence.repository.QuoteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class QuoteRepositoryAdapter implements QuoteRepositoryPort {

    private final QuoteJpaRepository jpaRepository;

    @Override
    public Quote save(Quote quote) {
        QuoteEntity entity = toEntity(quote);
        QuoteEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<Quote> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    private QuoteEntity toEntity(Quote quote) {
        return QuoteEntity.builder()
                .id(quote.getId())
                .productId(quote.getProductId())
                .offerId(quote.getOfferId())
                .category(quote.getCategory())
                .totalMonthlyPremiumAmount(quote.getTotalMonthlyPremiumAmount())
                .totalCoverageAmount(quote.getTotalCoverageAmount())
                .coverages(quote.getCoverages())
                .assistances(quote.getAssistances())
                .customer(quote.getCustomer())
                .policyId(quote.getPolicyId())
                .status(quote.getStatus())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }

    private Quote toDomain(QuoteEntity entity) {
        return Quote.builder()
                .id(entity.getId())
                .productId(entity.getProductId())
                .offerId(entity.getOfferId())
                .category(entity.getCategory())
                .totalMonthlyPremiumAmount(entity.getTotalMonthlyPremiumAmount())
                .totalCoverageAmount(entity.getTotalCoverageAmount())
                .coverages(entity.getCoverages())
                .assistances(entity.getAssistances())
                .customer(entity.getCustomer())
                .policyId(entity.getPolicyId())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
