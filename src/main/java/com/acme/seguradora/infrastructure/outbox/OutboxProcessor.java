package com.acme.seguradora.infrastructure.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxProcessor {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedDelayString = "${outbox.processor.interval-ms:5000}")
    @Transactional
    public void processOutboxEvents() {
        List<OutboxEvent> pendingEvents = outboxEventRepository
                .findByFlagSentFalseAndStatusOrderByDatCreatedAsc("PENDING");

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                Map<String, Object> payload = objectMapper.readValue(
                        event.getPayload(), new TypeReference<Map<String, Object>>() {});

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
