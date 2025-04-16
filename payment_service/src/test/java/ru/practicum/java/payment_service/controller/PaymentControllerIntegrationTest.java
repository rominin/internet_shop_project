package ru.practicum.java.payment_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@TestPropertySource(properties = {
        "payment.initial-balance=1000.00"
})
public class PaymentControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testMakePayment_withoutToken_shouldBeUnauthorized() {
        webTestClient
                .post()
                .uri("/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userId\": 1, \"amount\": 100.00}")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void testMakePayment_withInvalidClientId_shouldBeForbidden() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockJwt()
                        .jwt(jwt -> jwt.claim("client_id", "unknown_client")))
                .post()
                .uri("/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"userId\": 1, \"amount\": 100.00}")
                .exchange()
                .expectStatus().isForbidden();
    }
}