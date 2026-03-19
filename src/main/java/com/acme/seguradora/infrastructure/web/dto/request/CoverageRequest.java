package com.acme.seguradora.infrastructure.web.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoverageRequest {
    private String name;
    private BigDecimal value;
}
