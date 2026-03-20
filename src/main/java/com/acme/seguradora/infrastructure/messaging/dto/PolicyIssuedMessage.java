package com.acme.seguradora.infrastructure.messaging.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIssuedMessage {

    @JsonProperty("quote_id")
    private Long quoteId;

    @JsonProperty("policy_id")
    private Long policyId;
}
