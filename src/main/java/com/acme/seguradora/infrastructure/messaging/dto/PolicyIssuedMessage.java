package com.acme.seguradora.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PolicyIssuedMessage(
        @JsonProperty("quote_id") Long quoteId,
        @JsonProperty("policy_id") Long policyId) {}
