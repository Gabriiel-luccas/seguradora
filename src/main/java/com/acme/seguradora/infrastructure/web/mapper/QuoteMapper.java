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
        return new Quote(
                null,
                request.productId(),
                request.offerId(),
                request.category(),
                request.totalMonthlyPremiumAmount(),
                request.totalCoverageAmount(),
                toCoverageDomainList(request.coverages()),
                request.assistances(),
                toCustomerDomain(request.customer()),
                null, null, null, null);
    }

    public QuoteResponse toResponse(Quote quote) {
        return new QuoteResponse(
                quote.id(),
                quote.policyId(),
                quote.productId(),
                quote.offerId(),
                quote.category(),
                quote.createdAt(),
                quote.updatedAt(),
                quote.totalMonthlyPremiumAmount(),
                quote.totalCoverageAmount(),
                toCoverageResponseList(quote.coverages()),
                quote.assistances(),
                toCustomerResponse(quote.customer()),
                quote.status());
    }

    private List<Coverage> toCoverageDomainList(List<CoverageRequest> requests) {
        if (requests == null) return List.of();
        return requests.stream()
                .map(r -> new Coverage(r.name(), r.value()))
                .collect(Collectors.toList());
    }

    private Customer toCustomerDomain(CustomerRequest request) {
        if (request == null) return null;
        return new Customer(
                request.documentNumber(),
                request.name(),
                request.type(),
                request.gender(),
                request.dateOfBirth(),
                request.email(),
                request.phoneNumber());
    }

    private List<CoverageResponse> toCoverageResponseList(List<Coverage> coverages) {
        if (coverages == null) return List.of();
        return coverages.stream()
                .map(c -> new CoverageResponse(c.name(), c.value()))
                .collect(Collectors.toList());
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        if (customer == null) return null;
        return new CustomerResponse(
                customer.documentNumber(),
                customer.name(),
                customer.type(),
                customer.gender(),
                customer.dateOfBirth(),
                customer.email(),
                customer.phoneNumber());
    }
}
