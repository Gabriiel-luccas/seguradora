package com.acme.seguradora.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import org.hibernate.validator.constraints.br.CPF;

public record CustomerRequest(

        @JsonProperty("document_number")
        @CPF
        String documentNumber,

        String name,
        String type,
        String gender,

        @JsonProperty("date_of_birth")
        String dateOfBirth,

        @Email
        String email,

        @JsonProperty("phone_number")
        String phoneNumber) {}
