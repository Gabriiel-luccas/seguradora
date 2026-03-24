package com.acme.seguradora.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CustomerResponse(
        @JsonProperty("document_number") String documentNumber,
        String name,
        String type,
        String gender,
        @JsonProperty("date_of_birth") String dateOfBirth,
        String email,
        @JsonProperty("phone_number") String phoneNumber) {}
