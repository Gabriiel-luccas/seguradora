package com.acme.seguradora.infrastructure.messaging.publisher;

import com.acme.seguradora.application.port.output.MessagePublisherPort;
import com.acme.seguradora.domain.model.Coverage;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.config.KafkaConfig;
import com.acme.seguradora.infrastructure.messaging.dto.QuoteReceivedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class KafkaPublisher implements MessagePublisherPort {

    private static final Logger log = LoggerFactory.getLogger(KafkaPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KafkaPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public void publishQuoteReceived(Quote quote) {
        QuoteReceivedMessage message = new QuoteReceivedMessage(
                quote.id(),
                quote.productId(),
                quote.offerId(),
                quote.category(),
                quote.totalMonthlyPremiumAmount(),
                quote.totalCoverageAmount(),
                quote.coverages().stream().collect(Collectors.toMap(Coverage::name, Coverage::value)),
                quote.assistances(),
                quote.customer() != null ? quote.customer().documentNumber() : null,
                quote.customer() != null ? quote.customer().name() : null,
                quote.createdAt());

        log.info("Publishing quote.received event for quoteId={}", quote.id());
        kafkaTemplate.send(KafkaConfig.TOPIC_QUOTE_RECEIVED, String.valueOf(quote.id()), message);
    }
}
