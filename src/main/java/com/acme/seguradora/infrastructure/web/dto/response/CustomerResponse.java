package com.acme.seguradora.infrastructure.web.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {
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
