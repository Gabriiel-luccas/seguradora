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
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.Optional;

@Component
public class CatalogApiAdapter implements CatalogServicePort {

    private static final Logger log = LoggerFactory.getLogger(CatalogApiAdapter.class);
    private final WebClient catalogWebClient;

    public CatalogApiAdapter(WebClient catalogWebClient) {
        this.catalogWebClient = catalogWebClient;
    }

    @Override
    @Cacheable(value = "products", key = "#productId", unless = "#result == null")
    public Optional<CatalogProductDto> findProductById(String productId) {
        try {
            log.debug("Fetching product from catalog: {}", productId);
            ProductApiResponse response = catalogWebClient.get()
                    .uri("/catalog/products/{id}", productId)
                    .retrieve()
                    .bodyToMono(ProductApiResponse.class)
                    .block();

            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(new CatalogProductDto(
                    response.id(),
                    response.name(),
                    response.active(),
                    response.offersIds(),
                    response.createdAt()));
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Product not found in catalog: {}", productId);
            return Optional.empty();
        }
    }

    @Override
    @Cacheable(value = "offers", key = "#offerId", unless = "#result == null")
    public Optional<CatalogOfferDto> findOfferById(String offerId) {
        try {
            log.debug("Fetching offer from catalog: {}", offerId);
            OfferApiResponse response = catalogWebClient.get()
                    .uri("/catalog/offers/{id}", offerId)
                    .retrieve()
                    .bodyToMono(OfferApiResponse.class)
                    .block();

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
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Offer not found in catalog: {}", offerId);
            return Optional.empty();
        }
    }
}
