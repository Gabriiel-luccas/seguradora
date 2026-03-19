package com.acme.seguradora.infrastructure.messaging.consumer;

import com.acme.seguradora.application.service.QuoteService;
import com.acme.seguradora.infrastructure.config.RabbitMQConfig;
import com.acme.seguradora.infrastructure.messaging.dto.PolicyIssuedMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PolicyIssuedConsumer {

    private final QuoteService quoteService;

    @RabbitListener(queues = RabbitMQConfig.POLICY_ISSUED_QUEUE)
    public void consume(PolicyIssuedMessage message) {
        log.info("Received policy.issued message: policyId={} quoteId={}", message.getPolicyId(), message.getQuoteId());
        try {
            quoteService.updateQuoteWithPolicy(message.getQuoteId(), message.getPolicyId());
        } catch (Exception e) {
            log.error("Error processing policy.issued message for quoteId={}", message.getQuoteId(), e);
            throw e;
        }
    }
}
