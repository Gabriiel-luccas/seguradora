package com.acme.seguradora.infrastructure.outbox;

import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.config.KafkaConfig;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void saveQuoteReceivedEvent(Quote quote) {
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("quote_id", quote.getId());
            payload.put("product_id", quote.getProductId());
            payload.put("offer_id", quote.getOfferId());
            payload.put("category", quote.getCategory());
            payload.put("total_monthly_premium_amount", quote.getTotalMonthlyPremiumAmount());
            payload.put("total_coverage_amount", quote.getTotalCoverageAmount());
            payload.put("coverages", quote.getCoverages().stream()
                    .collect(Collectors.toMap(Coverage::getName, Coverage::getValue)));
            payload.put("assistances", quote.getAssistances());
            if (quote.getCustomer() != null) {
                payload.put("customer_document_number", quote.getCustomer().getDocumentNumber());
                payload.put("customer_name", quote.getCustomer().getName());
            }
            payload.put("created_at", quote.getCreatedAt() != null ? quote.getCreatedAt().toString() : LocalDateTime.now().toString());

            OutboxEvent event = OutboxEvent.builder()
                    .topic(KafkaConfig.TOPIC_QUOTE_RECEIVED)
                    .eventType("QUOTE_RECEIVED")
                    .payload(objectMapper.writeValueAsString(payload))
                    .quoteId(quote.getId())
                    .flagSent(false)
                    .status("PENDING")
                    .build();

            outboxEventRepository.save(event);
            log.debug("Saved outbox event for quoteId={}", quote.getId());
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload for quoteId={}", quote.getId(), e);
            throw new RuntimeException("Failed to serialize outbox payload", e);
        }
    }

    @Transactional
    public void markPolicyIssuedReceived(Long quoteId, Long policyId) {
        outboxEventRepository.findByFlagSentFalse().stream()
                .filter(e -> quoteId.equals(e.getQuoteId())
                        && "QUOTE_RECEIVED".equals(e.getEventType()))
                .forEach(e -> {
                    e.setDatReceived(LocalDateTime.now());
                    e.setDatUpdated(LocalDateTime.now());
                    outboxEventRepository.save(e);
                });

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("quote_id", quoteId);
            payload.put("policy_id", policyId);
            payload.put("received_at", LocalDateTime.now().toString());

            OutboxEvent receivedEvent = OutboxEvent.builder()
                    .topic(KafkaConfig.TOPIC_POLICY_ISSUED)
                    .eventType("POLICY_ISSUED")
                    .payload(objectMapper.writeValueAsString(payload))
                    .quoteId(quoteId)
                    .flagSent(true)
                    .datSent(LocalDateTime.now())
                    .datReceived(LocalDateTime.now())
                    .status("RECEIVED")
                    .build();

            outboxEventRepository.save(receivedEvent);
            log.debug("Saved POLICY_ISSUED outbox event for quoteId={}", quoteId);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize outbox payload for policyIssued quoteId={}", quoteId, e);
        }
    }
}
