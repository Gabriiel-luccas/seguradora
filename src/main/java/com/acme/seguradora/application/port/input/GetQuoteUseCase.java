package com.acme.seguradora.application.port.input;

import com.acme.seguradora.domain.model.Quote;

public interface GetQuoteUseCase {
    Quote getQuote(Long id);
}
