package com.acme.seguradora.integration;

import com.acme.seguradora.infrastructure.outbox.OutboxEventRepository;
import com.acme.seguradora.infrastructure.persistence.repository.QuoteJpaRepository;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.configureFor;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(
        partitions = 1,
        topics = {"quote.received", "policy.issued"},
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:0",
                "port=0"
        }
)
@ActiveProfiles("test")
@DirtiesContext
@DisplayName("Quote Controller Integration Tests")
class QuoteControllerIT {

    private static WireMockServer wireMockServer;

    @LocalServerPort
    private int port;

    @Autowired
    private QuoteJpaRepository quoteJpaRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @BeforeAll
    static void startWireMock() {
        wireMockServer = new WireMockServer(WireMockConfiguration.options().port(8089));
        wireMockServer.start();
        configureFor("localhost", 8089);
        setupWireMockStubs();
    }

    @AfterAll
    static void stopWireMock() {
        if (wireMockServer != null) {
            wireMockServer.stop();
        }
    }

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.basePath = "/api/v1";
        quoteJpaRepository.deleteAll();
        outboxEventRepository.deleteAll();
    }

    private static void setupWireMockStubs() {
        // Product stub
        stubFor(get(urlPathMatching("/catalog/products/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
                                  "name": "Seguro de Vida",
                                  "active": true,
                                  "offers_ids": ["aaa1b2c3-d4e5-6789-abcd-ef0123456789"]
                                }
                                """)));

        // Offer stub
        stubFor(get(urlPathMatching("/catalog/offers/.*"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
                                  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
                                  "name": "Oferta Padrão",
                                  "active": true,
                                  "coverages": {
                                    "Morte Acidental": 500000.00,
                                    "Invalidez Permanente": 300000.00,
                                    "Assistência Funeral": 25000.00
                                  },
                                  "assistances": ["Funeral", "Ambulância", "Chaveiro 24h"],
                                  "monthly_premium_amount": {
                                    "min_amount": 50.00,
                                    "max_amount": 200.00,
                                    "suggested_amount": 100.00
                                  }
                                }
                                """)));
    }

    // ======= POST /api/v1/quotes =======

    @Test
    @DisplayName("POST /quotes - valid request - should return 201 Created with quote")
    void createQuote_validRequest_shouldReturn201() {
        String requestBody = """
                {
                  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
                  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
                  "category": "LIFE",
                  "total_monthly_premium_amount": 75.25,
                  "total_coverage_amount": 825000.00,
                  "coverages": [
                    { "name": "Morte Acidental", "value": 500000.00 },
                    { "name": "Invalidez Permanente", "value": 300000.00 },
                    { "name": "Assistência Funeral", "value": 25000.00 }
                  ],
                  "assistances": ["Funeral", "Ambulância"],
                  "customer": {
                    "document_number": "36205578900",
                    "name": "John Doe",
                    "type": "NATURAL",
                    "gender": "MALE",
                    "date_of_birth": "1990-05-20",
                    "email": "john@example.com",
                    "phone_number": "11999999999"
                  }
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/quotes")
        .then()
                .statusCode(201)
                .contentType(ContentType.JSON)
                .body("id", notNullValue())
                .body("product_id", equalTo("1ab2c3d4-e5f6-7890-abcd-ef1234567890"))
                .body("offer_id", equalTo("aaa1b2c3-d4e5-6789-abcd-ef0123456789"))
                .body("category", equalTo("LIFE"))
                .body("status", equalTo("PENDING"))
                .body("total_monthly_premium_amount", equalTo(75.25f))
                .body("total_coverage_amount", equalTo(825000.00f))
                .body("coverages", hasSize(3))
                .body("customer.document_number", equalTo("36205578900"))
                .body("customer.name", equalTo("John Doe"))
                .body("created_at", notNullValue());
    }

    @Test
    @DisplayName("POST /quotes - missing required fields - should return 400 Bad Request")
    void createQuote_missingFields_shouldReturn400() {
        String requestBody = """
                {
                  "category": "LIFE"
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/quotes")
        .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"));
    }

    @Test
    @DisplayName("POST /quotes - product not found - should return 422 Unprocessable Entity")
    void createQuote_productNotFound_shouldReturn422() {
        // Override stub for this test: product returns 404
        wireMockServer.stubFor(get(urlPathMatching("/catalog/products/nonexistent.*"))
                .willReturn(aResponse()
                        .withStatus(404)
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"error\": \"Not Found\"}")));

        String requestBody = """
                {
                  "product_id": "nonexistent-product",
                  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
                  "category": "LIFE",
                  "total_monthly_premium_amount": 75.25,
                  "total_coverage_amount": 825000.00,
                  "coverages": [
                    { "name": "Morte Acidental", "value": 825000.00 }
                  ],
                  "customer": {
                    "document_number": "36205578900",
                    "name": "John Doe"
                  }
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/quotes")
        .then()
                .statusCode(422)
                .body("status", equalTo(422))
                .body("error", equalTo("Unprocessable Entity"));
    }

    @Test
    @DisplayName("POST /quotes - coverage exceeds max - should return 422 Unprocessable Entity")
    void createQuote_coverageExceedsMax_shouldReturn422() {
        String requestBody = """
                {
                  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
                  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
                  "category": "LIFE",
                  "total_monthly_premium_amount": 75.25,
                  "total_coverage_amount": 1025000.00,
                  "coverages": [
                    { "name": "Morte Acidental", "value": 1000000.00 },
                    { "name": "Assistência Funeral", "value": 25000.00 }
                  ],
                  "customer": {
                    "document_number": "36205578900",
                    "name": "John Doe"
                  }
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/quotes")
        .then()
                .statusCode(422)
                .body("status", equalTo(422));
    }

    // ======= GET /api/v1/quotes/{id} =======

    @Test
    @DisplayName("GET /quotes/{id} - existing quote - should return 200 OK")
    void getQuote_existingId_shouldReturn200() {
        // First create a quote
        String requestBody = """
                {
                  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
                  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
                  "category": "LIFE",
                  "total_monthly_premium_amount": 75.25,
                  "total_coverage_amount": 825000.00,
                  "coverages": [
                    { "name": "Morte Acidental", "value": 500000.00 },
                    { "name": "Invalidez Permanente", "value": 300000.00 },
                    { "name": "Assistência Funeral", "value": 25000.00 }
                  ],
                  "assistances": ["Funeral"],
                  "customer": {
                    "document_number": "36205578900",
                    "name": "Jane Doe"
                  }
                }
                """;

        Long quoteId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/quotes")
        .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        // Then retrieve it
        given()
        .when()
                .get("/quotes/{id}", quoteId)
        .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("id", equalTo(quoteId.intValue()))
                .body("product_id", equalTo("1ab2c3d4-e5f6-7890-abcd-ef1234567890"))
                .body("status", equalTo("PENDING"))
                .body("customer.name", equalTo("Jane Doe"))
                .body("created_at", notNullValue());
    }

    @Test
    @DisplayName("GET /quotes/{id} - non-existing quote - should return 404 Not Found")
    void getQuote_nonExistingId_shouldReturn404() {
        given()
        .when()
                .get("/quotes/{id}", 999999L)
        .then()
                .statusCode(404)
                .body("status", equalTo(404))
                .body("error", equalTo("Not Found"))
                .body("message", containsString("999999"));
    }

    @Test
    @DisplayName("POST /quotes - outbox event should be created after quote creation")
    void createQuote_shouldCreateOutboxEvent() {
        String requestBody = """
                {
                  "product_id": "1ab2c3d4-e5f6-7890-abcd-ef1234567890",
                  "offer_id": "aaa1b2c3-d4e5-6789-abcd-ef0123456789",
                  "category": "LIFE",
                  "total_monthly_premium_amount": 100.00,
                  "total_coverage_amount": 825000.00,
                  "coverages": [
                    { "name": "Morte Acidental", "value": 500000.00 },
                    { "name": "Invalidez Permanente", "value": 300000.00 },
                    { "name": "Assistência Funeral", "value": 25000.00 }
                  ],
                  "customer": {
                    "document_number": "12345678900",
                    "name": "Test User"
                  }
                }
                """;

        Long quoteId = given()
                .contentType(ContentType.JSON)
                .body(requestBody)
        .when()
                .post("/quotes")
        .then()
                .statusCode(201)
                .extract()
                .jsonPath()
                .getLong("id");

        // Verify outbox event was created
        var outboxEvents = outboxEventRepository.findByFlagSentFalse();
        Assertions.assertFalse(outboxEvents.isEmpty(), "Outbox event should be created");
        Assertions.assertEquals("quote.received", outboxEvents.get(0).getTopic());
        Assertions.assertEquals("QUOTE_RECEIVED", outboxEvents.get(0).getEventType());
        Assertions.assertEquals(quoteId, outboxEvents.get(0).getQuoteId());
        Assertions.assertFalse(outboxEvents.get(0).isFlagSent());
    }
}
