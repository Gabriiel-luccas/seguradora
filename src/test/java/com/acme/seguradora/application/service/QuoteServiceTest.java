package com.acme.seguradora.application.service;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.application.port.output.QuoteRepositoryPort;
import com.acme.seguradora.domain.exception.QuoteNotFoundException;
import com.acme.seguradora.domain.exception.QuoteValidationException;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.domain.model.QuoteStatus;
import com.acme.seguradora.infrastructure.outbox.OutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("QuoteService Unit Tests")
class QuoteServiceTest {

    @Mock
    private QuoteRepositoryPort quoteRepositoryPort;

    @Mock
    private CatalogServicePort catalogServicePort;

    @Mock
    private OutboxService outboxService;

    @InjectMocks
    private QuoteService quoteService;

    private CatalogProductDto activeProduct;
    private CatalogOfferDto activeOffer;
    private Quote validQuote;

    @BeforeEach
    void setUp() {
        activeProduct = new CatalogProductDto("prod-1", "Life Insurance", true, List.of("offer-1"), null);

        activeOffer = new CatalogOfferDto(
                "offer-1",
                "prod-1",
                "Standard Offer",
                true,
                Map.of(
                        "Morte Acidental", new BigDecimal("500000.00"),
                        "Invalidez Permanente", new BigDecimal("300000.00"),
                        "Assistência Funeral", new BigDecimal("25000.00")),
                List.of("Funeral", "Ambulância"),
                new BigDecimal("50.00"),
                new BigDecimal("200.00"),
                new BigDecimal("1000000.00"));

        validQuote = new Quote(
                null, "prod-1", "offer-1", "LIFE",
                new BigDecimal("75.25"), new BigDecimal("825000.00"),
                List.of(
                        new Coverage("Morte Acidental", new BigDecimal("500000.00")),
                        new Coverage("Invalidez Permanente", new BigDecimal("300000.00")),
                        new Coverage("Assistência Funeral", new BigDecimal("25000.00"))),
                List.of("Funeral", "Ambulância"),
                new Customer(
                        "36205578900",
                        "John Doe",
                        "NATURAL",
                        "MALE",
                        "1990-05-20",
                        "john@example.com",
                        "11999999999"),
                null, null, null, null);
    }

    @Test
    @DisplayName("createQuote - valid request - should save quote with PENDING status")
    void createQuote_validRequest_shouldReturnSavedQuote() {
        Quote savedQuote = new Quote(
                1L, validQuote.productId(), validQuote.offerId(), validQuote.category(),
                validQuote.totalMonthlyPremiumAmount(), validQuote.totalCoverageAmount(),
                validQuote.coverages(), validQuote.assistances(), validQuote.customer(),
                validQuote.policyId(), QuoteStatus.PENDING, LocalDateTime.now(), LocalDateTime.now());

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));
        when(quoteRepositoryPort.save(any(Quote.class))).thenReturn(savedQuote);
        doNothing().when(outboxService).enqueuePolicyAnalysisEvent(any(Quote.class));

        Quote result = quoteService.createQuote(validQuote);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(QuoteStatus.PENDING);

        verify(quoteRepositoryPort).save(any(Quote.class));
        verify(outboxService).enqueuePolicyAnalysisEvent(any(Quote.class));
    }

    @Test
    @DisplayName("createQuote - product not found - should throw QuoteValidationException")
    void createQuote_productNotFound_shouldThrowValidationException() {
        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.createQuote(validQuote))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Product not found");

        verifyNoInteractions(quoteRepositoryPort, outboxService);
    }

    @Test
    @DisplayName("createQuote - inactive product - should throw QuoteValidationException")
    void createQuote_inactiveProduct_shouldThrowValidationException() {
        CatalogProductDto inactiveProduct = new CatalogProductDto(activeProduct.id(), activeProduct.name(), false, activeProduct.offersIds(), activeProduct.createdAt());
        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(inactiveProduct));

        assertThatThrownBy(() -> quoteService.createQuote(validQuote))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("not active");
    }

    @Test
    @DisplayName("createQuote - offer not found - should throw QuoteValidationException")
    void createQuote_offerNotFound_shouldThrowValidationException() {
        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.createQuote(validQuote))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Offer not found");
    }

    @Test
    @DisplayName("createQuote - coverage exceeds maximum - should throw QuoteValidationException")
    void createQuote_coverageExceedsMaximum_shouldThrowValidationException() {
        Quote quoteCoverageExceeds = new Quote(
                validQuote.id(), validQuote.productId(), validQuote.offerId(), validQuote.category(),
                validQuote.totalMonthlyPremiumAmount(), new BigDecimal("925000.00"),
                List.of(
                        new Coverage("Morte Acidental", new BigDecimal("600000.00")),
                        new Coverage("Invalidez Permanente", new BigDecimal("300000.00")),
                        new Coverage("Assistência Funeral", new BigDecimal("25000.00"))),
                validQuote.assistances(), validQuote.customer(),
                validQuote.policyId(), validQuote.status(), validQuote.createdAt(), validQuote.updatedAt());

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteCoverageExceeds))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    @DisplayName("createQuote - invalid assistance - should throw QuoteValidationException")
    void createQuote_invalidAssistance_shouldThrowValidationException() {
        Quote quoteInvalidAssistance = new Quote(
                validQuote.id(), validQuote.productId(), validQuote.offerId(), validQuote.category(),
                validQuote.totalMonthlyPremiumAmount(), validQuote.totalCoverageAmount(),
                validQuote.coverages(), List.of("NonExistentAssistance"), validQuote.customer(),
                validQuote.policyId(), validQuote.status(), validQuote.createdAt(), validQuote.updatedAt());

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteInvalidAssistance))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Assistance not available");
    }

    @Test
    @DisplayName("createQuote - premium below minimum - should throw QuoteValidationException")
    void createQuote_premiumBelowMinimum_shouldThrowValidationException() {
        Quote quoteLowPremium = new Quote(
                validQuote.id(), validQuote.productId(), validQuote.offerId(), validQuote.category(),
                new BigDecimal("10.00"), validQuote.totalCoverageAmount(),
                validQuote.coverages(), validQuote.assistances(), validQuote.customer(),
                validQuote.policyId(), validQuote.status(), validQuote.createdAt(), validQuote.updatedAt());

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteLowPremium))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("below minimum");
    }

    @Test
    @DisplayName("createQuote - total coverage mismatch - should throw QuoteValidationException")
    void createQuote_totalCoverageMismatch_shouldThrowValidationException() {
        Quote quoteMismatch = new Quote(
                validQuote.id(), validQuote.productId(), validQuote.offerId(), validQuote.category(),
                validQuote.totalMonthlyPremiumAmount(), new BigDecimal("900000.00"),
                validQuote.coverages(), validQuote.assistances(), validQuote.customer(),
                validQuote.policyId(), validQuote.status(), validQuote.createdAt(), validQuote.updatedAt());

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteMismatch))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("does not match sum");
    }

    @Test
    @DisplayName("getQuote - existing id - should return quote")
    void getQuote_existingId_shouldReturnQuote() {
        Quote savedQuote = new Quote(
                1L, validQuote.productId(), validQuote.offerId(), validQuote.category(),
                validQuote.totalMonthlyPremiumAmount(), validQuote.totalCoverageAmount(),
                validQuote.coverages(), validQuote.assistances(), validQuote.customer(),
                validQuote.policyId(), QuoteStatus.PENDING, LocalDateTime.now(), validQuote.updatedAt());

        when(quoteRepositoryPort.findById(1L)).thenReturn(Optional.of(savedQuote));

        Quote result = quoteService.getQuote(1L);

        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getQuote - non-existing id - should throw QuoteNotFoundException")
    void getQuote_nonExistingId_shouldThrowNotFoundException() {
        when(quoteRepositoryPort.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.getQuote(999L))
                .isInstanceOf(QuoteNotFoundException.class)
                .hasMessageContaining("999");
    }

    @Test
    @DisplayName("updateQuoteWithPolicy - should update status to ACTIVE")
    void updateQuoteWithPolicy_shouldUpdateStatusToActive() {
        LocalDateTime now = LocalDateTime.now();
        Quote pendingQuote = new Quote(
                1L, validQuote.productId(), validQuote.offerId(), validQuote.category(),
                validQuote.totalMonthlyPremiumAmount(), validQuote.totalCoverageAmount(),
                validQuote.coverages(), validQuote.assistances(), validQuote.customer(),
                validQuote.policyId(), QuoteStatus.PENDING, now, validQuote.updatedAt());
        Quote activeQuote = new Quote(
                pendingQuote.id(), pendingQuote.productId(), pendingQuote.offerId(), pendingQuote.category(),
                pendingQuote.totalMonthlyPremiumAmount(), pendingQuote.totalCoverageAmount(),
                pendingQuote.coverages(), pendingQuote.assistances(), pendingQuote.customer(),
                100L, QuoteStatus.ACTIVE, pendingQuote.createdAt(), now);

        when(quoteRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingQuote));
        when(quoteRepositoryPort.save(any(Quote.class))).thenReturn(activeQuote);
        doNothing().when(outboxService).markPolicyIssuedReceived(anyLong());

        quoteService.updateQuoteWithPolicy(1L, 100L, now);

        verify(quoteRepositoryPort).save(argThat(q ->
                q.status() == QuoteStatus.ACTIVE && q.policyId().equals(100L)));
        verify(outboxService).markPolicyIssuedReceived(1L);
    }
}
