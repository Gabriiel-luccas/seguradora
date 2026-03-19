package com.acme.seguradora.application.service;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.application.port.output.MessagePublisherPort;
import com.acme.seguradora.application.port.output.QuoteRepositoryPort;
import com.acme.seguradora.domain.exception.QuoteNotFoundException;
import com.acme.seguradora.domain.exception.QuoteValidationException;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Customer;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.domain.model.QuoteStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuoteServiceTest {

    @Mock
    private QuoteRepositoryPort quoteRepository;

    @Mock
    private CatalogServicePort catalogService;

    @Mock
    private MessagePublisherPort messagePublisher;

    @InjectMocks
    private QuoteService quoteService;

    private static final String PRODUCT_ID = "1ab2c3d4-e5f6-7890-abcd-ef1234567890";
    private static final String OFFER_ID = "aaa1b2c3-d4e5-6789-abcd-ef0123456789";

    private CatalogProductDto activeProduct;
    private CatalogOfferDto activeOffer;
    private Quote validQuote;

    @BeforeEach
    void setUp() {
        activeProduct = CatalogProductDto.builder()
                .id(PRODUCT_ID)
                .name("Seguro de Vida")
                .active(true)
                .offerIds(List.of(OFFER_ID))
                .build();

        Map<String, BigDecimal> coverages = new LinkedHashMap<>();
        coverages.put("Morte Acidental", new BigDecimal("100000.00"));
        coverages.put("Invalidez Permanente", new BigDecimal("100000.00"));

        activeOffer = CatalogOfferDto.builder()
                .id(OFFER_ID)
                .productId(PRODUCT_ID)
                .name("Oferta Básica")
                .active(true)
                .coverages(coverages)
                .assistances(List.of("Assistência Funeral", "Telemedicina"))
                .monthlyPremium(CatalogOfferDto.MonthlyPremiumDto.builder()
                        .minAmount(new BigDecimal("50.00"))
                        .maxAmount(new BigDecimal("1000.00"))
                        .suggestedAmount(new BigDecimal("100.00"))
                        .build())
                .build();

        Customer customer = Customer.builder()
                .documentNumber("12345678901")
                .name("João Silva")
                .type("NATURAL_PERSON")
                .gender("MALE")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .email("joao@example.com")
                .phoneNumber("11999999999")
                .build();

        validQuote = Quote.builder()
                .productId(PRODUCT_ID)
                .offerId(OFFER_ID)
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("100.00"))
                .totalCoverageAmount(new BigDecimal("150000.00"))
                .coverages(List.of(
                        Coverage.builder().name("Morte Acidental").value(new BigDecimal("100000.00")).build(),
                        Coverage.builder().name("Invalidez Permanente").value(new BigDecimal("50000.00")).build()
                ))
                .assistances(List.of("Assistência Funeral"))
                .customer(customer)
                .build();
    }

    @Test
    void createQuote_validRequest_shouldReturnSavedQuote() {
        Quote savedQuote = Quote.builder()
                .id(1L)
                .productId(PRODUCT_ID)
                .offerId(OFFER_ID)
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("100.00"))
                .totalCoverageAmount(new BigDecimal("150000.00"))
                .coverages(validQuote.getCoverages())
                .assistances(validQuote.getAssistances())
                .customer(validQuote.getCustomer())
                .status(QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(catalogService.findOfferById(OFFER_ID)).thenReturn(Optional.of(activeOffer));
        when(quoteRepository.save(any(Quote.class))).thenReturn(savedQuote);

        Quote result = quoteService.createQuote(validQuote);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(QuoteStatus.PENDING);
        verify(messagePublisher).publishQuoteReceived(savedQuote);
    }

    @Test
    void createQuote_productNotFound_shouldThrowValidationException() {
        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.createQuote(validQuote))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    void createQuote_inactiveProduct_shouldThrowValidationException() {
        CatalogProductDto inactiveProduct = CatalogProductDto.builder()
                .id(PRODUCT_ID).name("Test").active(false).offerIds(List.of(OFFER_ID)).build();
        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(inactiveProduct));

        assertThatThrownBy(() -> quoteService.createQuote(validQuote))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("not active");
    }

    @Test
    void createQuote_offerNotFound_shouldThrowValidationException() {
        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(catalogService.findOfferById(OFFER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.createQuote(validQuote))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Offer not found");
    }

    @Test
    void createQuote_coverageExceedsMaximum_shouldThrowValidationException() {
        Quote quoteWithExcessiveCoverage = Quote.builder()
                .productId(PRODUCT_ID)
                .offerId(OFFER_ID)
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("100.00"))
                .totalCoverageAmount(new BigDecimal("200001.00"))
                .coverages(List.of(
                        Coverage.builder().name("Morte Acidental").value(new BigDecimal("200001.00")).build()
                ))
                .assistances(List.of())
                .customer(validQuote.getCustomer())
                .build();

        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(catalogService.findOfferById(OFFER_ID)).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteWithExcessiveCoverage))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("exceeds maximum");
    }

    @Test
    void createQuote_invalidAssistance_shouldThrowValidationException() {
        Quote quoteWithInvalidAssistance = Quote.builder()
                .productId(PRODUCT_ID)
                .offerId(OFFER_ID)
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("100.00"))
                .totalCoverageAmount(new BigDecimal("150000.00"))
                .coverages(validQuote.getCoverages())
                .assistances(List.of("Assistência Inválida"))
                .customer(validQuote.getCustomer())
                .build();

        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(catalogService.findOfferById(OFFER_ID)).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteWithInvalidAssistance))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Assistance not found");
    }

    @Test
    void createQuote_premiumBelowMinimum_shouldThrowValidationException() {
        Quote quoteWithLowPremium = Quote.builder()
                .productId(PRODUCT_ID)
                .offerId(OFFER_ID)
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("10.00"))
                .totalCoverageAmount(new BigDecimal("150000.00"))
                .coverages(validQuote.getCoverages())
                .assistances(List.of())
                .customer(validQuote.getCustomer())
                .build();

        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(catalogService.findOfferById(OFFER_ID)).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteWithLowPremium))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("monthly premium");
    }

    @Test
    void createQuote_totalCoverageMismatch_shouldThrowValidationException() {
        Quote quoteWithWrongTotal = Quote.builder()
                .productId(PRODUCT_ID)
                .offerId(OFFER_ID)
                .category("LIFE")
                .totalMonthlyPremiumAmount(new BigDecimal("100.00"))
                .totalCoverageAmount(new BigDecimal("999999.00"))
                .coverages(validQuote.getCoverages())
                .assistances(List.of())
                .customer(validQuote.getCustomer())
                .build();

        when(catalogService.findProductById(PRODUCT_ID)).thenReturn(Optional.of(activeProduct));
        when(catalogService.findOfferById(OFFER_ID)).thenReturn(Optional.of(activeOffer));

        assertThatThrownBy(() -> quoteService.createQuote(quoteWithWrongTotal))
                .isInstanceOf(QuoteValidationException.class)
                .hasMessageContaining("Total coverage amount");
    }

    @Test
    void getQuote_existingId_shouldReturnQuote() {
        Quote quote = Quote.builder().id(1L).status(QuoteStatus.PENDING).build();
        when(quoteRepository.findById(1L)).thenReturn(Optional.of(quote));

        Quote result = quoteService.getQuote(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getQuote_nonExistingId_shouldThrowNotFoundException() {
        when(quoteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> quoteService.getQuote(99L))
                .isInstanceOf(QuoteNotFoundException.class);
    }

    @Test
    void updateQuoteWithPolicy_shouldUpdateStatusToActive() {
        Quote existingQuote = Quote.builder()
                .id(1L)
                .status(QuoteStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(quoteRepository.findById(1L)).thenReturn(Optional.of(existingQuote));
        when(quoteRepository.save(any(Quote.class))).thenAnswer(inv -> inv.getArgument(0));

        quoteService.updateQuoteWithPolicy(1L, 100L);

        verify(quoteRepository).save(argThat(q ->
                q.getPolicyId().equals(100L) && q.getStatus() == QuoteStatus.ACTIVE
        ));
    }
}
