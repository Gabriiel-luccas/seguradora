package com.acme.seguradora.application.service;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.input.CreateQuoteUseCase;
import com.acme.seguradora.application.port.input.GetQuoteUseCase;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.application.port.output.MessagePublisherPort;
import com.acme.seguradora.application.port.output.QuoteRepositoryPort;
import com.acme.seguradora.domain.exception.QuoteNotFoundException;
import com.acme.seguradora.domain.exception.QuoteValidationException;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.domain.model.QuoteStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QuoteService implements CreateQuoteUseCase, GetQuoteUseCase {

    private final QuoteRepositoryPort quoteRepository;
    private final CatalogServicePort catalogService;
    private final MessagePublisherPort messagePublisher;

    @Override
    public Quote createQuote(Quote quote) {
        log.info("Creating quote for product={} offer={}", quote.getProductId(), quote.getOfferId());

        CatalogProductDto product = catalogService.findProductById(quote.getProductId())
                .orElseThrow(() -> new QuoteValidationException("Product not found: " + quote.getProductId()));

        if (!product.isActive()) {
            throw new QuoteValidationException("Product is not active: " + quote.getProductId());
        }

        CatalogOfferDto offer = catalogService.findOfferById(quote.getOfferId())
                .orElseThrow(() -> new QuoteValidationException("Offer not found: " + quote.getOfferId()));

        if (!offer.isActive()) {
            throw new QuoteValidationException("Offer is not active: " + quote.getOfferId());
        }

        if (!product.getOfferIds().contains(quote.getOfferId())) {
            throw new QuoteValidationException("Offer does not belong to product");
        }

        validateCoverages(quote.getCoverages(), offer);
        validateAssistances(quote.getAssistances(), offer);
        validateMonthlyPremium(quote.getTotalMonthlyPremiumAmount(), offer);
        validateTotalCoverageAmount(quote);

        quote.setStatus(QuoteStatus.PENDING);
        quote.setCreatedAt(LocalDateTime.now());
        quote.setUpdatedAt(LocalDateTime.now());
        Quote savedQuote = quoteRepository.save(quote);

        messagePublisher.publishQuoteReceived(savedQuote);

        log.info("Quote created with id={}", savedQuote.getId());
        return savedQuote;
    }

    @Override
    public Quote getQuote(Long id) {
        return quoteRepository.findById(id)
                .orElseThrow(() -> new QuoteNotFoundException(id));
    }

    private void validateCoverages(List<Coverage> requestedCoverages, CatalogOfferDto offer) {
        if (requestedCoverages == null || requestedCoverages.isEmpty()) {
            throw new QuoteValidationException("Coverages list cannot be empty");
        }
        for (Coverage coverage : requestedCoverages) {
            BigDecimal maxAllowed = offer.getCoverages().get(coverage.getName());
            if (maxAllowed == null) {
                throw new QuoteValidationException("Coverage not found in offer: " + coverage.getName());
            }
            if (coverage.getValue().compareTo(maxAllowed) > 0) {
                throw new QuoteValidationException(
                        "Coverage value exceeds maximum for " + coverage.getName() +
                        ": max=" + maxAllowed + " requested=" + coverage.getValue());
            }
        }
    }

    private void validateAssistances(List<String> requestedAssistances, CatalogOfferDto offer) {
        if (requestedAssistances == null || requestedAssistances.isEmpty()) {
            return;
        }
        for (String assistance : requestedAssistances) {
            if (!offer.getAssistances().contains(assistance)) {
                throw new QuoteValidationException("Assistance not found in offer: " + assistance);
            }
        }
    }

    private void validateMonthlyPremium(BigDecimal totalMonthlyPremium, CatalogOfferDto offer) {
        CatalogOfferDto.MonthlyPremiumDto mp = offer.getMonthlyPremium();
        if (totalMonthlyPremium.compareTo(mp.getMinAmount()) < 0 ||
            totalMonthlyPremium.compareTo(mp.getMaxAmount()) > 0) {
            throw new QuoteValidationException(
                    "Total monthly premium " + totalMonthlyPremium +
                    " is not within allowed range [" + mp.getMinAmount() + ", " + mp.getMaxAmount() + "]");
        }
    }

    private void validateTotalCoverageAmount(Quote quote) {
        BigDecimal sum = quote.getCoverages().stream()
                .map(Coverage::getValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (sum.compareTo(quote.getTotalCoverageAmount()) != 0) {
            throw new QuoteValidationException(
                    "Total coverage amount " + quote.getTotalCoverageAmount() +
                    " does not match sum of coverages " + sum);
        }
    }

    public void updateQuoteWithPolicy(Long quoteId, Long policyId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> new QuoteNotFoundException(quoteId));
        quote.setPolicyId(policyId);
        quote.setStatus(QuoteStatus.ACTIVE);
        quote.setUpdatedAt(LocalDateTime.now());
        quoteRepository.save(quote);
        log.info("Quote {} updated with policyId={}", quoteId, policyId);
    }
}
