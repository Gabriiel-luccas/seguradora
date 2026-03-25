package com.acme.seguradora.application.port.output;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;

import java.util.Optional;

public interface CatalogServicePort {
    Optional<CatalogProductDto> findProductById(String productId);
    Optional<CatalogOfferDto> findOfferById(String offerId);
}
