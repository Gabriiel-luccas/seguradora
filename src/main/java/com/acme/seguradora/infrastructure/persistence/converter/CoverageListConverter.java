package com.acme.seguradora.infrastructure.persistence.converter;

import com.acme.seguradora.domain.model.Coverage;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class CoverageListConverter extends JsonConverter<List<Coverage>> {
    public CoverageListConverter() {
        super(new TypeReference<List<Coverage>>() {});
    }
}
