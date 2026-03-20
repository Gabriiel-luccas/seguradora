package com.acme.seguradora.application.service;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.input.CreateQuoteUseCase;
import com.acme.seguradora.application.port.input.GetQuoteUseCase;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.application.port.output.QuoteRepositoryPort;
import com.acme.seguradora.domain.exception.QuoteNotFoundException;
import com.acme.seguradora.domain.exception.QuoteValidationException;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.domain.model.QuoteStatus;
import com.acme.seguradora.infrastructure.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService implements CreateQuoteUseCase, GetQuoteUseCase {

    private final QuoteRepositoryPort quoteRepositoryPort;
    private final CatalogServicePort catalogServicePort;
    private final OutboxService outboxService;

    @Override
    @Transactional
    public Quote createQuote(Quote quote) {
        log.info("Creating quote for product={}, offer={}", quote.getProductId(), quote.getOfferId());

        CatalogProductDto product = catalogServicePort.findProductById(quote.getProductId())
                .orElseThrow(() -> new QuoteValidationException(
                        "Product not found: " + quote.getProductId()));

        if (!product.isActive()) {
            throw new QuoteValidationException("Product is not active: " + quote.getProductId());
        }

        CatalogOfferDto offer = catalogServicePort.findOfferById(quote.getOfferId())
                .orElseThrow(() -> new QuoteValidationException(
                        "Offer not found: " + quote.getOfferId()));

        if (!offer.isActive()) {
            throw new QuoteValidationException("Offer is not active: " + quote.getOfferId());
        }

        if (!product.getOffersIds().contains(offer.getId())) {
            throw new QuoteValidationException(
                    "Offer " + quote.getOfferId() + " does not belong to product " + quote.getProductId());
        }

        validateCoverages(quote.getCoverages(), offer);
        validateAssistances(quote.getAssistances(), offer);
        validateMonthlyPremium(quote.getTotalMonthlyPremiumAmount(), offer);
        validateTotalCoverageAmount(quote);

        quote.setStatus(QuoteStatus.PENDING);
        quote.setCreatedAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());

        Quote savedQuote = quoteRepositoryPort.save(quote);

        outboxService.saveQuoteReceivedEvent(savedQuote);

        log.info("Quote created with id={}", savedQuote.getId());
        return savedQuote;
    }

    @Override
    public Quote getQuote(Long id) {
        return quoteRepositoryPort.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));
    }

    @Transactional
    public void updateQuoteWithPolicy(Long quoteId, Long policyId) {
        log.info("Updating quote id={} with policyId={}", quoteId, policyId);
        Quote quote = quoteRepositoryPort.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

        quote.setPolicyId(policyId);
        quote.setStatus(QuoteStatus.ACTIVE);
        quote.setUpdatedAt(LocalDateTime.now());

        quoteRepositoryPort.save(quote);

        outboxService.markPolicyIssuedReceived(quoteId, policyId);

        log.info("Quote id={} updated to ACTIVE with policyId={}", quoteId, policyId);
    }

    private void validateCoverages(List<Coverage> coverages, CatalogOfferDto offer) {
        for (Coverage coverage : coverages) {
            BigDecimal maxValue = offer.getCoverages().get(coverage.getName());
            if (maxValue == null) {
                throw new QuoteValidationException(
                        "Coverage not found in offer: " + coverage.getName());
            }
            if (coverage.getValue().compareTo(maxValue) > 0) {
                throw new QuoteValidationException(
                        "Coverage value exceeds maximum for " + coverage.getName()
                                + ": max=" + maxValue + ", requested=" + coverage.getValue());
            }
        }
    }

    private void validateAssistances(List<String> assistances, CatalogOfferDto offer) {
        if (assistances == null || assistances.isEmpty()) {
            return;
        }
        for (String assistance : assistances) {
            if (!offer.getAssistances().contains(assistance)) {
                throw new QuoteValidationException(
                        "Assistance not available in offer: " + assistance);
            }
        }
    }

    private void validateMonthlyPremium(BigDecimal premium, CatalogOfferDto offer) {
        if (premium.compareTo(offer.getMinMonthlyPremiumAmount()) < 0) {
            throw new QuoteValidationException(
                    "Monthly premium " + premium + " is below minimum " + offer.getMinMonthlyPremiumAmount());
        }
        if (premium.compareTo(offer.getMaxMonthlyPremiumAmount()) > 0) {
            throw new QuoteValidationException(
                    "Monthly premium " + premium + " exceeds maximum " + offer.getMaxMonthlyPremiumAmount());
        }
    }

    private void validateTotalCoverageAmount(Quote quote) {
        BigDecimal sum = quote.getCoverages().stream()
                .map(Coverage::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(quote.getTotalCoverageAmount()) != 0) {
            throw new QuoteValidationException(
                    "Total coverage amount " + quote.getTotalCoverageAmount()
                            + " does not match sum of coverages " + sum);
        }
    }
}
