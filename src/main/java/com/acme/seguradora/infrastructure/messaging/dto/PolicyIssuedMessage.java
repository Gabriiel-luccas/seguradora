package com.acme.seguradora.infrastructure.messaging.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PolicyIssuedMessage {
    private Long policyId;
    private Long quoteId;
    private LocalDateTime issuedAt;
    private String status;
    private String insuredName;
}
