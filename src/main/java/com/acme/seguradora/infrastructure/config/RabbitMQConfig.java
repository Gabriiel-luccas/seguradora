package com.acme.seguradora.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "insurance.exchange";
    public static final String QUOTE_RECEIVED_QUEUE = "quote.received";
    public static final String POLICY_ISSUED_QUEUE = "policy.issued";
    public static final String QUOTE_RECEIVED_ROUTING_KEY = "quote.received";
    public static final String POLICY_ISSUED_ROUTING_KEY = "policy.issued";

    @Bean
    public TopicExchange insuranceExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue quoteReceivedQueue() {
        return QueueBuilder.durable(QUOTE_RECEIVED_QUEUE).build();
    }

    @Bean
    public Queue policyIssuedQueue() {
        return QueueBuilder.durable(POLICY_ISSUED_QUEUE).build();
    }

    @Bean
    public Binding quoteReceivedBinding(Queue quoteReceivedQueue, TopicExchange insuranceExchange) {
        return BindingBuilder.bind(quoteReceivedQueue).to(insuranceExchange).with(QUOTE_RECEIVED_ROUTING_KEY);
    }

    @Bean
    public Binding policyIssuedBinding(Queue policyIssuedQueue, TopicExchange insuranceExchange) {
        return BindingBuilder.bind(policyIssuedQueue).to(insuranceExchange).with(POLICY_ISSUED_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(mapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }
}
