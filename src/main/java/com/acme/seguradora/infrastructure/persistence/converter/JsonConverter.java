package com.acme.seguradora.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JsonConverter<T> {

    private static final Logger log = LoggerFactory.getLogger(JsonConverter.class);
    protected static final ObjectMapper MAPPER = new ObjectMapper();

    protected String toJson(T object) {
        if (object == null) return null;
        try {
            return MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON", e);
            throw new RuntimeException("Error converting object to JSON", e);
        }
    }

    protected T fromJson(String json, Class<T> clazz) {
        if (json == null || json.isBlank()) return null;
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to object", e);
            throw new RuntimeException("Error converting JSON to object", e);
        }
    }
}
