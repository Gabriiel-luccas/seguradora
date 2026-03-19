package com.acme.seguradora.infrastructure.persistence.repository;

import com.acme.seguradora.infrastructure.persistence.entity.QuoteEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuoteJpaRepository extends JpaRepository<QuoteEntity, Long> {
}
