package com.acme.seguradora.infrastructure.persistence.converter;

import com.acme.seguradora.domain.model.Customer;
import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.Converter;

@Converter
public class CustomerConverter extends JsonConverter<Customer> {
    public CustomerConverter() {
        super(new TypeReference<Customer>() {});
    }
}
