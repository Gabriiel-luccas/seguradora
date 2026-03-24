package com.acme.seguradora.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Component
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxProcessor(OutboxEventRepository outboxEventRepository,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.processor.interval-ms:5000}")
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEventEntity> pendingEvents = outboxEventRepository
                .findByFlagSentFalseAndStatusOrderByDatCreatedAsc("PENDING");

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        event.getPayload(), new TypeReference<>() {
                        });

                String key = event.getQuoteId() != null ? String.valueOf(event.getQuoteId()) : null;
                kafkaTemplate.send(event.getTopic(), key, payload).get();

                event.setFlagSent(true);
                event.setDatSent(LocalDateTime.now());
                event.setStatus("SENT");
                event.setDatUpdated(LocalDateTime.now());
                outboxEventRepository.save(event);

                log.info("Published outbox event id={}, topic={}, quoteId={}",
                        event.getId(), event.getTopic(), event.getQuoteId());

            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), e.getMessage());
                event.setRetryCount(event.getRetryCount() + 1);
                event.setErrorMessage(e.getMessage());
                event.setDatUpdated(LocalDateTime.now());

                if (event.getRetryCount() >= 3) {
                    event.setStatus("FAILED");
                    log.error("Outbox event id={} marked as FAILED after {} retries",
                            event.getId(), event.getRetryCount());
                }
                outboxEventRepository.save(event);
            }
        }
    }
}
