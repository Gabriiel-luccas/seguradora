package com.acme.seguradora.infrastructure.persistence.mapper;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.persistence.entity.AssistanceEntity;
import com.acme.seguradora.infrastructure.persistence.entity.CoverageEntity;
import com.acme.seguradora.infrastructure.persistence.entity.CustomerEntity;
import com.acme.seguradora.infrastructure.persistence.entity.QuoteEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuoteEntityMapper {

    public QuoteEntity toEntity(Quote quote) {
        QuoteEntity entity = QuoteEntity.builder()
                .id(quote.id())
                .productId(quote.productId())
                .offerId(quote.offerId())
                .category(quote.category())
                .totalMonthlyPremiumAmount(quote.totalMonthlyPremiumAmount())
                .totalCoverageAmount(quote.totalCoverageAmount())
                .customer(toCustomerEntity(quote.customer()))
                .policyId(quote.policyId())
                .status(quote.status())
                .createdAt(quote.createdAt())
                .updatedAt(quote.updatedAt())
                .build();

        List<CoverageEntity> coverages = quote.coverages().stream()
                .map(c -> {
                    CoverageEntity ce = new CoverageEntity(c.name(), c.value());
                    ce.setQuote(entity);
                    return ce;
                }).toList();

        List<AssistanceEntity> assistances = quote.assistances().stream()
                .map(a -> {
                    AssistanceEntity ae = new AssistanceEntity(a);
                    ae.setQuote(entity);
                    return ae;
                }).toList();

        entity.setCoverages(coverages);
        entity.setAssistances(assistances);

        return entity;
    }

    /**
     * Updates only the fields that change during policy reception.
     * Coverages, assistances and customer are immutable after quote creation.
     */
    public void updateEntity(QuoteEntity entity, Quote quote) {
        entity.setPolicyId(quote.policyId());
        entity.setStatus(quote.status());
    }

    public Quote toDomain(QuoteEntity entity) {
        List<Coverage> coverages = entity.getCoverages().stream()
                .map(c -> new Coverage(c.getName(), c.getValue()))
                .toList();

        List<String> assistances = entity.getAssistances().stream()
                .map(AssistanceEntity::getName)
                .toList();

        CustomerEntity ce = entity.getCustomer();
        Customer customer = new Customer(
                ce.getDocumentNumber(), ce.getName(), ce.getType(),
                ce.getGender(), ce.getDateOfBirth(), ce.getEmail(), ce.getPhoneNumber());

        return new Quote(
                entity.getId(),
                entity.getProductId(),
                entity.getOfferId(),
                entity.getCategory(),
                entity.getTotalMonthlyPremiumAmount(),
                entity.getTotalCoverageAmount(),
                coverages,
                assistances,
                customer,
                entity.getPolicyId(),
                entity.getStatus(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }

    private CustomerEntity toCustomerEntity(Customer c) {
        return new CustomerEntity(
                c.documentNumber(), c.name(), c.type(),
                c.gender(), c.dateOfBirth(), c.email(), c.phoneNumber());
    }
}
