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
        activeProduct = CatalogProductDto.builder()
                .id("prod-1")
                .name("Life Insurance")
                .active(true)
                .offersIds(List.of("offer-1"))
                .build();

        activeOffer = CatalogOfferDto.builder()
                .id("offer-1")
                .productId("prod-1")
                .name("Standard Offer")
                .active(true)
                .coverages(Map.of(
                        "Morte Acidental", new BigDecimal("500000.00"),
                        "Invalidez Permanente", new BigDecimal("300000.00"),
                        "Assistência Funeral", new BigDecimal("25000.00")))
                .assistances(List.of("Funeral", "Ambulância"))
                .minMonthlyPremiumAmount(new BigDecimal("50.00"))
                .maxMonthlyPremiumAmount(new BigDecimal("200.00"))
                .maxCoverageAmount(new BigDecimal("1000000.00"))
                .build();

        validQuote = Quote.builder()
                .productId("prod-1")
                .offerId("offer-1")
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("75.25"))
                .totalCoverageAmount(new BigDecimal("825000.00"))
                .coverages(List.of(
                        Coverage.builder().name("Morte Acidental").value(new BigDecimal("500000.00")).build(),
                        Coverage.builder().name("Invalidez Permanente").value(new BigDecimal("300000.00")).build(),
                        Coverage.builder().name("Assistência Funeral").value(new BigDecimal("25000.00")).build()))
                .assistances(List.of("Funeral", "Ambulância"))
                .customer(Customer.builder()
                        .documentNumber("36205578900")
                        .name("John Doe")
                        .type("NATURAL")
                        .gender("MALE")
                        .dateOfBirth("1990-05-20")
                        .email("john@example.com")
                        .phoneNumber("11999999999")
                        .build())
                .build();
    }

    @Test
    @DisplayName("createQuote - valid request - should save quote with PENDING status")
    void createQuote_validRequest_shouldReturnSavedQuote() {
        Quote savedQuote = validQuote.toBuilder()
                .id(1L)
                .status(QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));
        when(quoteRepositoryPort.save(any(Quote.class))).thenReturn(savedQuote);
        doNothing().when(outboxService).saveQuoteReceivedEvent(any(Quote.class));

        Quote result = quoteService.createQuote(validQuote);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(QuoteStatus.PENDING);

        verify(quoteRepositoryPort).save(any(Quote.class));
        verify(outboxService).saveQuoteReceivedEvent(any(Quote.class));
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
        CatalogProductDto inactiveProduct = activeProduct.toBuilder().active(false).build();
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
        Quote quoteCoverageExceeds = validQuote.toBuilder()
                .coverages(List.of(
                        Coverage.builder().name("Morte Acidental").value(new BigDecimal("600000.00")).build(),
                        Coverage.builder().name("Invalidez Permanente").value(new BigDecimal("300000.00")).build(),
                        Coverage.builder().name("Assistência Funeral").value(new BigDecimal("25000.00")).build()))
                .totalCoverageAmount(new BigDecimal("925000.00"))
                .build();

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteCoverageExceeds))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    @DisplayName("createQuote - invalid assistance - should throw QuoteValidationException")
    void createQuote_invalidAssistance_shouldThrowValidationException() {
        Quote quoteInvalidAssistance = validQuote.toBuilder()
                .assistances(List.of("NonExistentAssistance"))
                .build();

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteInvalidAssistance))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Assistance not available");
    }

    @Test
    @DisplayName("createQuote - premium below minimum - should throw QuoteValidationException")
    void createQuote_premiumBelowMinimum_shouldThrowValidationException() {
        Quote quoteLowPremium = validQuote.toBuilder()
                .totalMonthlyPremiumAmount(new BigDecimal("10.00"))
                .build();

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteLowPremium))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("below minimum");
    }

    @Test
    @DisplayName("createQuote - total coverage mismatch - should throw QuoteValidationException")
    void createQuote_totalCoverageMismatch_shouldThrowValidationException() {
        Quote quoteMismatch = validQuote.toBuilder()
                .totalCoverageAmount(new BigDecimal("900000.00"))
                .build();

        when(catalogServicePort.findProductById("prod-1")).thenReturn(Optional.of(activeProduct));
        when(catalogServicePort.findOfferById("offer-1")).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteMismatch))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("does not match sum");
    }

    @Test
    @DisplayName("getQuote - existing id - should return quote")
    void getQuote_existingId_shouldReturnQuote() {
        Quote savedQuote = validQuote.toBuilder()
                .id(1L)
                .status(QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        when(quoteRepositoryPort.findById(1L)).thenReturn(Optional.of(savedQuote));

        Quote result = quoteService.getQuote(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
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
        Quote pendingQuote = validQuote.toBuilder()
                .id(1L)
                .status(QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        Quote activeQuote = pendingQuote.toBuilder()
                .policyId(100L)
                .status(QuoteStatus.ACTIVE)
                .build();

        when(quoteRepositoryPort.findById(1L)).thenReturn(Optional.of(pendingQuote));
        when(quoteRepositoryPort.save(any(Quote.class))).thenReturn(activeQuote);
        doNothing().when(outboxService).markPolicyIssuedReceived(anyLong(), anyLong());

        quoteService.updateQuoteWithPolicy(1L, 100L);

        verify(quoteRepositoryPort).save(argThat(q ->
                q.getStatus() == QuoteStatus.ACTIVE && q.getPolicyId().equals(100L)));
        verify(outboxService).markPolicyIssuedReceived(1L, 100L);
    }
}
