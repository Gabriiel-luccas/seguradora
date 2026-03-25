package com.acme.seguradora.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OutboxEventRepository extends JpaRepository<OutboxEventEntity, Long> {

    List<OutboxEventEntity> findByFlagSentFalseAndStatusOrderByDatCreatedAsc(String status);

    List<OutboxEventEntity> findByFlagSentFalse();

    Optional<OutboxEventEntity> findByQuoteIdAndEventType(Long quoteId, String eventType);
}
