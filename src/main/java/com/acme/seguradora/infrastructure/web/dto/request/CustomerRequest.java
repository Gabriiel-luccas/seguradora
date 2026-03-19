package com.acme.seguradora.infrastructure.web.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CustomerRequest {
    @NotBlank
    @JsonProperty("document_number")
    private String documentNumber;
    @NotBlank
    private String name;
    private String type;
    private String gender;
    @Past
    @JsonProperty("date_of_birth")
    private LocalDate dateOfBirth;
    @Email
    private String email;
    @JsonProperty("phone_number")
    private String phoneNumber;
}
