package com.acme.seguradora.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

@Configuration
public class RestClientConfig {

    @Value("${catalog.api.base-url}")
    private String catalogBaseUrl;

    @Bean
    public RestTemplate catalogRestTemplate(RestTemplateBuilder builder) {
        return builder
                .rootUri(catalogBaseUrl)
                .build();
    }
}
