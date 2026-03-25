package com.acme.seguradora.infrastructure.persistence.converter;

import com.acme.seguradora.domain.model.Customer;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class CustomerConverter extends JsonConverter<Customer>
        implements AttributeConverter<Customer, String> {

    @Override
    public String convertToDatabaseColumn(Customer customer) {
        return toJson(customer);
    }

    @Override
    public Customer convertToEntityAttribute(String json) {
        return fromJson(json, Customer.class);
    }
}
