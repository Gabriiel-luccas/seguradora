package com.acme.seguradora.infrastructure.web.mapper;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.web.dto.request.CreateQuoteRequest;
import com.acme.seguradora.infrastructure.web.dto.request.CoverageRequest;
import com.acme.seguradora.infrastructure.web.dto.request.CustomerRequest;
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
                .coverages(toCoverageDomainList(request.getCoverages()))
                .assistances(request.getAssistances())
                .customer(toCustomerDomain(request.getCustomer()))
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
                .coverages(toCoverageResponseList(quote.getCoverages()))
                .assistances(quote.getAssistances())
                .customer(toCustomerResponse(quote.getCustomer()))
                .policyId(quote.getPolicyId())
                .status(quote.getStatus())
                .createdAt(quote.getCreatedAt())
                .updatedAt(quote.getUpdatedAt())
                .build();
    }

    private List<Coverage> toCoverageDomainList(List<CoverageRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream()
                .map(r -> Coverage.builder().name(r.getName()).value(r.getValue()).build())
                .collect(Collectors.toList());
    }

    private Customer toCustomerDomain(CustomerRequest request) {
        if (request == null) return null;
        return Customer.builder()
                .documentNumber(request.getDocumentNumber())
                .name(request.getName())
                .type(request.getType())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .build();
    }

    private List<CoverageResponse> toCoverageResponseList(List<Coverage> coverages) {
        if (coverages == null) return List.of();
        return coverages.stream()
                .map(c -> CoverageResponse.builder().name(c.getName()).value(c.getValue()).build())
                .collect(Collectors.toList());
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) return null;
        return CustomerResponse.builder()
                .documentNumber(customer.getDocumentNumber())
                .name(customer.getName())
                .type(customer.getType())
                .gender(customer.getGender())
                .dateOfBirth(customer.getDateOfBirth())
                .email(customer.getEmail())
                .phoneNumber(customer.getPhoneNumber())
                .build();
    }
}
