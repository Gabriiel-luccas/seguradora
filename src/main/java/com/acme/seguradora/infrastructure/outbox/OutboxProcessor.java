package com.acme.seguradora.infrastructure.outbox;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Component
public class OutboxProcessor {

    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);
    private static final long KAFKA_SEND_TIMEOUT_SECONDS = 5;

    private final OutboxService outboxService;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public OutboxProcessor(OutboxService outboxService,
                           KafkaTemplate<String, Object> kafkaTemplate,
                           ObjectMapper objectMapper) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelayString = "${outbox.processor.interval-ms:5000}")
    public void processOutboxEvents() {
        // Load events in a short-lived read-only transaction — DB connection released immediately
        List<OutboxEventEntity> pendingEvents = outboxService.loadPendingEvents();

        if (pendingEvents.isEmpty()) {
            log.debug("no pending outbox events");
            return;
        }

        log.info("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEventEntity event : pendingEvents) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        event.getPayload(), new TypeReference<>() {});

                String key = String.valueOf(event.getQuoteId());

                // Kafka I/O is outside any DB transaction — no connection held during network I/O
                kafkaTemplate.send(event.getTopic(), key, payload)
                        .get(KAFKA_SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

                // Each update runs in its own short transaction via OutboxService proxy
                outboxService.markEventSent(event.getId());

                log.info("Published outbox event id={}, topic={}, quoteId={}",
                        event.getId(), event.getTopic(), event.getQuoteId());

            } catch (TimeoutException e) {
                log.error("Timeout sending outbox event id={} to Kafka after {}s — will retry",
                        event.getId(), KAFKA_SEND_TIMEOUT_SECONDS);
                outboxService.incrementRetry(event.getId(),
                        "Kafka send timed out after " + KAFKA_SEND_TIMEOUT_SECONDS + "s");
            } catch (Exception e) {
                log.error("Failed to publish outbox event id={}: {}", event.getId(), e.getMessage());
                outboxService.incrementRetry(event.getId(), e.getMessage());
            }
        }
    }
}
