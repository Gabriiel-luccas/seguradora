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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class QuoteService implements CreateQuoteUseCase, GetQuoteUseCase {

    private static final Logger log = LoggerFactory.getLogger(QuoteService.class);

    private final QuoteRepositoryPort quoteRepositoryPort;
    private final CatalogServicePort catalogServicePort;
    private final OutboxService outboxService;

    public QuoteService(QuoteRepositoryPort quoteRepositoryPort, CatalogServicePort catalogServicePort, OutboxService outboxService) {
        this.quoteRepositoryPort = quoteRepositoryPort;
        this.catalogServicePort = catalogServicePort;
        this.outboxService = outboxService;
    }

    @Override
    @Transactional
    public Quote createQuote(Quote quote) {
        log.info("Creating quote for product={}, offer={}", quote.productId(), quote.offerId());

        CatalogProductDto product = catalogServicePort.findProductById(quote.productId())
                .orElseThrow(() -> new QuoteValidationException(
                        "Product not found: " + quote.productId()));

        if (!product.active()) {
            throw new QuoteValidationException("Product is not active: " + quote.productId());
        }

        CatalogOfferDto offer = catalogServicePort.findOfferById(quote.offerId())
                .orElseThrow(() -> new QuoteValidationException(
                        "Offer not found: " + quote.offerId()));

        if (!offer.active()) {
            throw new QuoteValidationException("Offer is not active: " + quote.offerId());
        }

        if (!product.offersIds().contains(offer.id())) {
            throw new QuoteValidationException(
                    "Offer " + quote.offerId() + " does not belong to product " + quote.productId());
        }

        validateCoverages(quote.coverages(), offer);
        validateAssistances(quote.assistances(), offer);
        validateMonthlyPremium(quote.totalMonthlyPremiumAmount(), offer);
        validateTotalCoverageAmount(quote);

        Quote pendingQuote = new Quote(
                quote.id(), quote.productId(), quote.offerId(), quote.category(),
                quote.totalMonthlyPremiumAmount(), quote.totalCoverageAmount(),
                quote.coverages(), quote.assistances(), quote.customer(),
                quote.policyId(), QuoteStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        Quote savedQuote = quoteRepositoryPort.save(pendingQuote);

        outboxService.saveQuoteReceivedEvent(savedQuote);

        log.info("Quote created with id={}", savedQuote.id());
        return savedQuote;
    }

    @Override
    public Quote getQuote(Long id) {
        return quoteRepositoryPort.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));
    }

    @Transactional
    public void updateQuoteWithPolicy(Long quoteId, Long policyId, LocalDateTime receivedAt) {
        log.info("Updating quote id={} with policyId={} receivedAt={}", quoteId, policyId, receivedAt);

        Quote existing = quoteRepositoryPort.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));

        Quote updatedQuote = new Quote(
                existing.id(), existing.productId(), existing.offerId(), existing.category(),
                existing.totalMonthlyPremiumAmount(), existing.totalCoverageAmount(),
                existing.coverages(), existing.assistances(), existing.customer(),
                policyId, QuoteStatus.ACTIVE, existing.createdAt(), receivedAt);

        quoteRepositoryPort.save(updatedQuote);

        outboxService.markPolicyIssuedReceived(quoteId);

        log.info("Quote id={} updated to ACTIVE with policyId={}", quoteId, policyId);
    }

    private void validateCoverages(List<Coverage> coverages, CatalogOfferDto offer) {
        for (Coverage coverage : coverages) {
            BigDecimal maxValue = offer.coverages().get(coverage.name());
            if (maxValue == null) {
                throw new QuoteValidationException(
                        "Coverage not found in offer: " + coverage.name());
            }
            if (coverage.value().compareTo(maxValue) > 0) {
                throw new QuoteValidationException(
                        "Coverage value exceeds maximum for " + coverage.name()
                                + ": max=" + maxValue + ", requested=" + coverage.value());
            }
        }
    }

    private void validateAssistances(List<String> assistances, CatalogOfferDto offer) {
        if (assistances == null || assistances.isEmpty()) {
            return;
        }
        for (String assistance : assistances) {
            if (!offer.assistances().contains(assistance)) {
                throw new QuoteValidationException(
                        "Assistance not available in offer: " + assistance);
            }
        }
    }

    private void validateMonthlyPremium(BigDecimal premium, CatalogOfferDto offer) {
        if (premium.compareTo(offer.minMonthlyPremiumAmount()) < 0) {
            throw new QuoteValidationException(
                    "Monthly premium " + premium + " is below minimum " + offer.minMonthlyPremiumAmount());
        }
        if (premium.compareTo(offer.maxMonthlyPremiumAmount()) > 0) {
            throw new QuoteValidationException(
                    "Monthly premium " + premium + " exceeds maximum " + offer.maxMonthlyPremiumAmount());
        }
    }

    private void validateTotalCoverageAmount(Quote quote) {
        BigDecimal sum = quote.coverages().stream()
                .map(Coverage::value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (sum.compareTo(quote.totalCoverageAmount()) != 0) {
            throw new QuoteValidationException(
                    "Total coverage amount " + quote.totalCoverageAmount()
                            + " does not match sum of coverages " + sum);
        }
    }
}
