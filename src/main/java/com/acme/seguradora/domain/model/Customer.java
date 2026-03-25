package com.acme.seguradora.domain.model;

public record Customer(
        String documentNumber,
        String name,
        String type,
        String gender,
        String dateOfBirth,
        String email,
        String phoneNumber) {}
