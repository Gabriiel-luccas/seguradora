package com.acme.seguradora.infrastructure.web.controller;

import com.acme.seguradora.application.port.input.CreateQuoteUseCase;
import com.acme.seguradora.application.port.input.GetQuoteUseCase;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.web.dto.request.CreateQuoteRequest;
import com.acme.seguradora.infrastructure.web.dto.response.QuoteResponse;
import com.acme.seguradora.infrastructure.web.mapper.QuoteMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final CreateQuoteUseCase createQuoteUseCase;
    private final GetQuoteUseCase getQuoteUseCase;
    private final QuoteMapper quoteMapper;

    @PostMapping
    public ResponseEntity<QuoteResponse> createQuote(
            @Valid @RequestBody CreateQuoteRequest request) {
        log.info("POST /api/v1/quotes - productId={}, offerId={}", request.getProductId(), request.getOfferId());
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
