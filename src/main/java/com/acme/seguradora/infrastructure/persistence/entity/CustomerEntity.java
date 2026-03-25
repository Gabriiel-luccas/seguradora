package com.acme.seguradora.infrastructure.persistence.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "customers")
public class CustomerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq")
    @SequenceGenerator(name = "customer_seq", sequenceName = "customer_sequence", allocationSize = 1)
    private Long id;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false, length = 20)
    private String type;

    @Column(name = "gender", length = 20)
    private String gender;

    @Column(name = "date_of_birth", length = 20)
    private String dateOfBirth;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    protected CustomerEntity() {}

    public CustomerEntity(String documentNumber, String name, String type,
                          String gender, String dateOfBirth, String email, String phoneNumber) {
        this.documentNumber = documentNumber;
        this.name = name;
        this.type = type;
        this.gender = gender;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public Long getId() { return id; }
    public String getDocumentNumber() { return documentNumber; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getGender() { return gender; }
    public String getDateOfBirth() { return dateOfBirth; }
    public String getEmail() { return email; }
    public String getPhoneNumber() { return phoneNumber; }
}

