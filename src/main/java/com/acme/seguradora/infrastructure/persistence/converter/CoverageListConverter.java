package com.acme.seguradora.infrastructure.persistence.converter;

import com.acme.seguradora.domain.model.Coverage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@Slf4j
@Converter
public class CoverageListConverter extends JsonConverter<List<Coverage>>
        implements AttributeConverter<List<Coverage>, String> {

    @Override
    public String convertToDatabaseColumn(List<Coverage> coverages) {
        return toJson(coverages);
    }

    @Override
    public List<Coverage> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<Coverage>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to Coverage list", e);
            return Collections.emptyList();
        }
    }
}
