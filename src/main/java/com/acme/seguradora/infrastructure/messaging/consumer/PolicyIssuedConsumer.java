package com.acme.seguradora.infrastructure.messaging.consumer;

import com.acme.seguradora.application.service.QuoteService;
import com.acme.seguradora.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;

@Component
public class PolicyIssuedConsumer {

    private static final Logger log = LoggerFactory.getLogger(PolicyIssuedConsumer.class);

    private final QuoteService quoteService;
    private final ObjectMapper objectMapper;

    public PolicyIssuedConsumer(QuoteService quoteService, ObjectMapper objectMapper) {
        this.quoteService = quoteService;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = KafkaConfig.TOPIC_POLICY_ISSUED,
            groupId = KafkaConfig.CONSUMER_GROUP_ID,
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(Map<String, Object> payload) {
        try {
            log.info("Received policy.issued event: {}", payload);

            Long quoteId = toLong(payload.get("quote_id"));
            Long policyId = toLong(payload.get("policy_id"));
            LocalDateTime createdAt = (LocalDateTime) payload.get("created_at");

            if (quoteId == null || policyId == null) {
                log.error("Invalid policy.issued message: missing quote_id or policy_id");
                return;
            }

            LocalDateTime receivedAt = createdAt != null ? createdAt : LocalDateTime.now();
            quoteService.updateQuoteWithPolicy(quoteId, policyId, receivedAt);
        } catch (Exception e) {
            log.error("Error processing policy.issued event: {}", e.getMessage(), e);
            throw e;
        }
    }

    private Long toLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long l) return l;
        if (value instanceof Integer i) return i.longValue();
        if (value instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
