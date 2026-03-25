package com.acme.seguradora.infrastructure.web.dto.response;

import java.math.BigDecimal;

public record CoverageResponse(String name, BigDecimal value) {}
