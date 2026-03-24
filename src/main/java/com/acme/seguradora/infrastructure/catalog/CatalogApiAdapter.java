package com.acme.seguradora.infrastructure.catalog;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.infrastructure.catalog.dto.OfferApiResponse;
import com.acme.seguradora.infrastructure.catalog.dto.ProductApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Component
public class CatalogApiAdapter implements CatalogServicePort {

    private static final Logger log = LoggerFactory.getLogger(CatalogApiAdapter.class);
    private final RestTemplate catalogRestTemplate;

    public CatalogApiAdapter(RestTemplate catalogRestTemplate) {
        this.catalogRestTemplate = catalogRestTemplate;
    }

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
            return Optional.of(new CatalogProductDto(
                    response.id(),
                    response.name(),
                    response.active(),
                    response.offersIds(),
                    response.createdAt()));
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

            return Optional.of(new CatalogOfferDto(
                    response.id(),
                    response.productId(),
                    response.name(),
                    response.active(),
                    response.coverages(),
                    response.assistances(),
                    response.monthlyPremiumAmount().minAmount(),
                    response.monthlyPremiumAmount().maxAmount(),
                    response.monthlyPremiumAmount().suggestedAmount()));
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Offer not found in catalog: {}", offerId);
            return Optional.empty();
        }
    }
}
