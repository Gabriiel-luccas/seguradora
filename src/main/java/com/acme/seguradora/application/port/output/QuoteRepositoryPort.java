package com.acme.seguradora.application.port.output;

import com.acme.seguradora.domain.model.Quote;

import java.util.Optional;

public interface QuoteRepositoryPort {
    Quote save(Quote quote);
    Optional<Quote> findById(Long id);
}
