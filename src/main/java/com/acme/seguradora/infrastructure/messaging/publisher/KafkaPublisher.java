package com.acme.seguradora.infrastructure.messaging.publisher;

import com.acme.seguradora.application.port.output.MessagePublisherPort;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.config.KafkaConfig;
import com.acme.seguradora.infrastructure.messaging.dto.QuoteReceivedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaPublisher implements MessagePublisherPort {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Override
    public void publishQuoteReceived(Quote quote) {
        QuoteReceivedMessage message = QuoteReceivedMessage.builder()
                .quoteId(quote.getId())
                .productId(quote.getProductId())
                .offerId(quote.getOfferId())
                .category(quote.getCategory())
                .totalMonthlyPremiumAmount(quote.getTotalMonthlyPremiumAmount())
                .totalCoverageAmount(quote.getTotalCoverageAmount())
                .coverages(quote.getCoverages().stream()
                        .collect(Collectors.toMap(Coverage::getName, Coverage::getValue)))
                .assistances(quote.getAssistances())
                .customerDocumentNumber(
                        quote.getCustomer() != null ? quote.getCustomer().getDocumentNumber() : null)
                .customerName(
                        quote.getCustomer() != null ? quote.getCustomer().getName() : null)
                .createdAt(quote.getCreatedAt())
                .build();

        log.info("Publishing quote.received event for quoteId={}", quote.getId());
        kafkaTemplate.send(KafkaConfig.TOPIC_QUOTE_RECEIVED, String.valueOf(quote.getId()), message);
    }
}
