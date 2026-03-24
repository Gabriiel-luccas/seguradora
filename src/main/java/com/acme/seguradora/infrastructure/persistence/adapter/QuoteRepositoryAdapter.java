package com.acme.seguradora.infrastructure.persistence.adapter;

import com.acme.seguradora.application.port.output.QuoteRepositoryPort;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.persistence.mapper.QuoteEntityMapper;
import com.acme.seguradora.infrastructure.persistence.repository.QuoteJpaRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class QuoteRepositoryAdapter implements QuoteRepositoryPort {

    private final QuoteJpaRepository quoteJpaRepository;
    private final QuoteEntityMapper mapper;

    public QuoteRepositoryAdapter(QuoteJpaRepository quoteJpaRepository, QuoteEntityMapper mapper) {
        this.quoteJpaRepository = quoteJpaRepository;
        this.mapper = mapper;
    }

    @Override
    public Quote save(Quote quote) {
        return mapper.toDomain(quoteJpaRepository.save(mapper.toEntity(quote)));
    }

    @Override
    public Optional<Quote> findById(Long id) {
        return quoteJpaRepository.findById(id).map(mapper::toDomain);
    }
}

