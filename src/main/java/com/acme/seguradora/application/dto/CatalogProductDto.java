package com.acme.seguradora.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CatalogProductDto {

    private String id;
    private String name;
    private boolean active;
    private List<String> offersIds;
}
