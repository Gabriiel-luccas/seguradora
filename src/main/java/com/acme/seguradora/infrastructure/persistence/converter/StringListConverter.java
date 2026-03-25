package com.acme.seguradora.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;

@Converter
public class StringListConverter extends JsonConverter<List<String>>
        implements AttributeConverter<List<String>, String> {

    private static final Logger log = LoggerFactory.getLogger(StringListConverter.class);

    @Override
    public String convertToDatabaseColumn(List<String> list) {
        return toJson(list);
    }

    @Override
    public List<String> convertToEntityAttribute(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            return MAPPER.readValue(json, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to String list", e);
            return Collections.emptyList();
        }
    }
}
