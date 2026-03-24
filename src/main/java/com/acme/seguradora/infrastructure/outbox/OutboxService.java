package com.acme.seguradora.infrastructure.outbox;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OutboxService {

    private static final Logger log = LoggerFactory.getLogger(OutboxService.class);
    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public OutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveQuoteReceivedEvent(Quote quote) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("quote_id", quote.id());
            payload.put("product_id", quote.productId());
            payload.put("offer_id", quote.offerId());
            payload.put("category", quote.category());
            payload.put("total_monthly_premium_amount", quote.totalMonthlyPremiumAmount());
            payload.put("total_coverage_amount", quote.totalCoverageAmount());
            payload.put("coverages", quote.coverages().stream()
                    .collect(Collectors.toMap(Coverage::name, Coverage::value)));
            payload.put("assistances", quote.assistances());
            if (quote.customer() != null) {
                payload.put("customer_document_number", quote.customer().documentNumber());
                payload.put("customer_name", quote.customer().name());
            }
            payload.put("created_at", quote.createdAt() != null ? quote.createdAt().toString() : LocalDateTime.now().toString());

            OutboxEventEntity event = OutboxEventEntity.builder()
                    .topic(KafkaConfig.TOPIC_QUOTE_RECEIVED)
                    .eventType("QUOTE_RECEIVED")
                    .payload(objectMapper.writeValueAsString(payload))
                    .quoteId(quote.id())
                    .flagSent(false)
                    .status("PENDING")
                    .build();

            outboxEventRepository.save(event);
            log.debug("Saved outbox event for quoteId={}", quote.id());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload for quoteId={}", quote.id(), e);
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }

    @Transactional
    public void markPolicyIssuedReceived(Long quoteId) {
        Optional<OutboxEventEntity> byQuoteIdAndEventType = outboxEventRepository
                .findByQuoteIdAndEventType(quoteId, "QUOTE_RECEIVED");

        if (byQuoteIdAndEventType.isEmpty()) {
            log.warn("No QUOTE_RECEIVED outbox event found for quoteId={}", quoteId);
            return;
        }
        LocalDateTime now = LocalDateTime.now();
        OutboxEventEntity outboxEventEntity = byQuoteIdAndEventType.get();
        outboxEventEntity.setDatReceived(now);
        outboxEventEntity.setStatus("RECEIVED");
        outboxEventEntity.setDatUpdated(now);
        outboxEventRepository.save(outboxEventEntity);

        log.debug("Marked QUOTE_RECEIVED event as RECEIVED for quoteId={}", quoteId);
    }

    @Transactional(readOnly = true)
    public java.util.List<OutboxEventEntity> loadPendingEvents() {
        return outboxEventRepository.findByFlagSentFalseAndStatusOrderByDatCreatedAsc("PENDING");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markEventSent(Long eventId) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setFlagSent(true);
            event.setDatSent(LocalDateTime.now());
            event.setStatus("SENT");
            event.setDatUpdated(LocalDateTime.now());
            outboxEventRepository.save(event);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void incrementRetry(Long eventId, String errorMessage) {
        outboxEventRepository.findById(eventId).ifPresent(event -> {
            event.setRetryCount(event.getRetryCount() + 1);
            event.setErrorMessage(errorMessage);
            event.setDatUpdated(LocalDateTime.now());
            if (event.getRetryCount() >= 3) {
                event.setStatus("FAILED");
                log.error("Outbox event id={} marked as FAILED after {} retries",
                        event.getId(), event.getRetryCount());
            }
            outboxEventRepository.save(event);
        });
    }
}
