package com.acme.seguradora.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {
    @JsonProperty("document_number")
    private String documentNumber;
    private String name;
    private String type;
    private String gender;
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    private String email;
    @JsonProperty("phone_number")
    private String phoneNumber;
}
