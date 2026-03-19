package com.acme.seguradora.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
    private String documentNumber;
    private String name;
    private String type;
    private String gender;
    private LocalDate dateOfBirth;
    private String email;
    private String phoneNumber;
}
