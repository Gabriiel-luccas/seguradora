package com.acme.seguradora.infrastructure.catalog.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductApiResponse {
    private String id;
    private String name;
    private boolean active;
    @JsonProperty("offer_ids")
    private List<String> offerIds;
}
