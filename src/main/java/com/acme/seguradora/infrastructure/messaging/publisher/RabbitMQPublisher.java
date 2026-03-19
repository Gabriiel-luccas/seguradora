package com.acme.seguradora.infrastructure.messaging.publisher;

import com.acme.seguradora.application.port.output.MessagePublisherPort;
import com.acme.seguradora.domain.model.Quote;
import com.acme.seguradora.infrastructure.config.RabbitMQConfig;
import com.acme.seguradora.infrastructure.messaging.dto.QuoteReceivedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQPublisher implements MessagePublisherPort {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void publishQuoteReceived(Quote quote) {
        QuoteReceivedMessage message = QuoteReceivedMessage.builder()
                .quoteId(quote.getId())
                .productId(quote.getProductId())
                .offerId(quote.getOfferId())
                .category(quote.getCategory())
                .totalMonthlyPremiumAmount(quote.getTotalMonthlyPremiumAmount())
                .totalCoverageAmount(quote.getTotalCoverageAmount())
                .coverages(quote.getCoverages())
                .assistances(quote.getAssistances())
                .customer(quote.getCustomer())
                .createdAt(quote.getCreatedAt())
                .build();

        log.info("Publishing quote.received message for quoteId={}", quote.getId());
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.EXCHANGE_NAME,
                RabbitMQConfig.QUOTE_RECEIVED_ROUTING_KEY,
                message
        );
    }
}
