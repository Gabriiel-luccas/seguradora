package com.acme.seguradora.infrastructure.web.controller;

import com.acme.seguradora.application.port.input.CreateQuoteUseCase;
import com.acme.seguradora.application.port.input.GetQuoteUseCase;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.web.dto.request.CreateQuoteRequest;
import com.acme.seguradora.infrastructure.web.dto.response.QuoteResponse;
import com.acme.seguradora.infrastructure.web.mapper.QuoteMapper;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
public class QuoteController {

    private static final Logger log = LoggerFactory.getLogger(QuoteController.class);

    private final CreateQuoteUseCase createQuoteUseCase;
    private final GetQuoteUseCase getQuoteUseCase;
    private final QuoteMapper quoteMapper;

    public QuoteController(CreateQuoteUseCase createQuoteUseCase, GetQuoteUseCase getQuoteUseCase, QuoteMapper quoteMapper) {
        this.createQuoteUseCase = createQuoteUseCase;
        this.getQuoteUseCase = getQuoteUseCase;
        this.quoteMapper = quoteMapper;
    }

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(
            @Valid @RequestBody CreateQuoteRequest request) {
        log.info("criando solicitacao de cotacao de seguro - productId={}, offerId={}", request.productId(), request.offerId());
        Quote quote = quoteMapper.toDomain(request);
        Quote savedQuote = createQuoteUseCase.createQuote(quote);
        return ResponseEntity.status(HttpStatus.CREATED).body(quoteMapper.toResponse(savedQuote));
    }

    @GetMapping("/{id}")
    public ResponseEntity<QuoteResponse> getQuote(@PathVariable Long id) {
        log.info("GET /api/v1/quotes/{}", id);
        Quote quote = getQuoteUseCase.getQuote(id);
        return ResponseEntity.ok(quoteMapper.toResponse(quote));
    }
}
