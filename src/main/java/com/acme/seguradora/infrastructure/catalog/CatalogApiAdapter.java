package com.acme.seguradora.infrastructure.catalog;

import com.acme.seguradora.application.dto.CatalogOfferDto;
import com.acme.seguradora.application.dto.CatalogProductDto;
import com.acme.seguradora.application.port.output.CatalogServicePort;
import com.acme.seguradora.infrastructure.catalog.dto.OfferApiResponse;
import com.acme.seguradora.infrastructure.catalog.dto.ProductApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class CatalogApiAdapter implements CatalogServicePort {

    private final RestTemplate catalogRestTemplate;

    @Value("${catalog.api.base-url}")
    private String baseUrl;

    @Override
    @Cacheable(value = "products", key = "#productId")
    public Optional<CatalogProductDto> findProductById(String productId) {
        try {
            log.info("Fetching product from catalog: {}", productId);
            ProductApiResponse response = catalogRestTemplate.getForObject(
                    UriComponentsBuilder.fromHttpUrl(baseUrl)
                            .pathSegment("catalog", "products", productId)
                            .build().toUriString(),
                    ProductApiResponse.class
            );
            if (response == null) return Optional.empty();
            return Optional.of(CatalogProductDto.builder()
                    .id(response.getId())
                    .name(response.getName())
                    .active(response.isActive())
                    .offerIds(response.getOfferIds())
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
            log.info("Fetching offer from catalog: {}", offerId);
            OfferApiResponse response = catalogRestTemplate.getForObject(
                    UriComponentsBuilder.fromHttpUrl(baseUrl)
                            .pathSegment("catalog", "offers", offerId)
                            .build().toUriString(),
                    OfferApiResponse.class
            );
            if (response == null) return Optional.empty();
            return Optional.of(CatalogOfferDto.builder()
                    .id(response.getId())
                    .productId(response.getProductId())
                    .name(response.getName())
                    .active(response.isActive())
                    .coverages(response.getCoverages())
                    .assistances(response.getAssistances())
                    .monthlyPremium(CatalogOfferDto.MonthlyPremiumDto.builder()
                            .minAmount(response.getMonthlyPremium().getMinAmount())
                            .maxAmount(response.getMonthlyPremium().getMaxAmount())
                            .suggestedAmount(response.getMonthlyPremium().getSuggestedAmount())
                            .build())
                    .build());
        } catch (HttpClientErrorException.NotFound e) {
            log.warn("Offer not found in catalog: {}", offerId);
            return Optional.empty();
        }
    }
}
