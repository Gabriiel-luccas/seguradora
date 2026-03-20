package com.acme.seguradora.infrastructure.catalog;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.infrastructure.catalog.dto.OfferApiResponse;
import com.acme.seguradora.infrastructure.catalog.dto.ProductApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogApiAdapter implements CatalogServicePort {

    private final RestTemplate catalogRestTemplate;

    @Override
    @Cacheable(value = "products", key = "#productId")
    public Optional<CatalogProductDto> findProductById(String productId) {
        try {
            log.debug("Fetching product from catalog: {}", productId);
            ProductApiResponse response = catalogRestTemplate.getForObject(
                    "/catalog/products/{id}", ProductApiResponse.class, productId);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(CatalogProductDto.builder()
                    .id(response.getId())
                    .name(response.getName())
                    .active(response.isActive())
                    .offersIds(response.getOffersIds())
                    .build());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Product not found in catalog: {}", productId);
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(value = "offers", key = "#offerId")
    public Optional<CatalogOfferDto> findOfferById(String offerId) {
        try {
            log.debug("Fetching offer from catalog: {}", offerId);
            OfferApiResponse response = catalogRestTemplate.getForObject(
                    "/catalog/offers/{id}", OfferApiResponse.class, offerId);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(CatalogOfferDto.builder()
                    .id(response.getId())
                    .productId(response.getProductId())
                    .name(response.getName())
                    .active(response.isActive())
                    .coverages(response.getCoverages())
                    .assistances(response.getAssistances())
                    .minMonthlyPremiumAmount(response.getMinMonthlyPremiumAmount())
                    .maxMonthlyPremiumAmount(response.getMaxMonthlyPremiumAmount())
                    .maxCoverageAmount(response.getMaxCoverageAmount())
                    .build());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Offer not found in catalog: {}", offerId);
            return Optional.empty();
        }
    }
}
