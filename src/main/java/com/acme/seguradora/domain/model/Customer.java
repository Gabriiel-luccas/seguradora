package com.acme.seguradora.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private String documentNumber;
    private String name;
    private String type;
    private String gender;
    private String dateOfBirth;
    private String email;
    private String phoneNumber;
}
