package com.acme.seguradora.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
@Converter
public class JsonConverter<T> implements AttributeConverter<T, String> {

    protected static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    private final TypeReference<T> typeReference;

    public JsonConverter(TypeReference<T> typeReference) {
        this.typeReference = typeReference;
    }

    @Override
    public String convertToDatabaseColumn(T attribute) {
        if (attribute == null) return null;
        try {
            return MAPPER.writeValueAsString(attribute);
        } catch (Exception e) {
            throw new IllegalStateException("Error converting attribute to JSON: " + attribute, e);
        }
    }

    @Override
    public T convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        try {
            return MAPPER.readValue(dbData, typeReference);
        } catch (Exception e) {
            throw new IllegalStateException("Error converting JSON to entity attribute. Raw data: " + dbData, e);
        }
    }
}
