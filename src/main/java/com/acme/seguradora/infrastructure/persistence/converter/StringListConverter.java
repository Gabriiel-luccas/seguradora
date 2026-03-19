package com.acme.seguradora.infrastructure.persistence.converter;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

import java.util.List;

@Converter
public class StringListConverter extends JsonConverter<List<String>> {
    public StringListConverter() {
        super(new TypeReference<List<String>>() {});
    }
}
