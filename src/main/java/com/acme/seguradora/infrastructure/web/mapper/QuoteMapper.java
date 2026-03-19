package com.acme.seguradora.infrastructure.web.mapper;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.web.dto.request.CreateQuoteRequest;
import com.acme.seguradora.infrastructure.web.dto.response.CoverageResponse;
import com.acme.seguradora.infrastructure.web.dto.response.CustomerResponse;
import com.acme.seguradora.infrastructure.web.dto.response.QuoteResponse;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class QuoteMapper {

    public Quote toDomain(CreateQuoteRequest request) {
        return Quote.builder()
                .productId(request.getProductId())
                .offerId(request.getOfferId())
                .category(request.getCategory())
                .totalMonthlyPremiumAmount(request.getTotalMonthlyPremiumAmount())
                .totalCoverageAmount(request.getTotalCoverageAmount())
                .coverages(request.getCoverages().stream()
                        .map(c -> Coverage.builder().name(c.getName()).value(c.getValue()).build())
                        .collect(Collectors.toList()))
                .assistances(request.getAssistances())
                .customer(request.getCustomer() != null ? Customer.builder()
                        .documentNumber(request.getCustomer().getDocumentNumber())
                        .name(request.getCustomer().getName())
                        .type(request.getCustomer().getType())
                        .gender(request.getCustomer().getGender())
                        .dateOfBirth(request.getCustomer().getDateOfBirth())
                        .email(request.getCustomer().getEmail())
                        .phoneNumber(request.getCustomer().getPhoneNumber())
                        .build() : null)
                .build();
    }

    public QuoteResponse toResponse(Quote quote) {
        return QuoteResponse.builder()
                .id(quote.getId())
                .productId(quote.getProductId())
                .offerId(quote.getOfferId())
                .category(quote.getCategory())
                .totalMonthlyPremiumAmount(quote.getTotalMonthlyPremiumAmount())
                .totalCoverageAmount(quote.getTotalCoverageAmount())
                .coverages(quote.getCoverages() == null ? null : quote.getCoverages().stream()
                        .map(c -> CoverageResponse.builder().name(c.getName()).value(c.getValue()).build())
                        .collect(Collectors.toList()))
                .assistances(quote.getAssistances())
                .customer(quote.getCustomer() != null ? CustomerResponse.builder()
                        .documentNumber(quote.getCustomer().getDocumentNumber())
                        .name(quote.getCustomer().getName())
                        .type(quote.getCustomer().getType())
                        .gender(quote.getCustomer().getGender())
                        .dateOfBirth(quote.getCustomer().getDateOfBirth())
                        .email(quote.getCustomer().getEmail())
                        .phoneNumber(quote.getCustomer().getPhoneNumber())
                        .build() : null)
                .policyId(quote.getPolicyId())
                .status(quote.getStatus())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }
}
