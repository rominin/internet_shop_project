package ru.practicum.java.payment_service.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
    void testMakePayment_success() {
        webTestClient.post()
                .uri("/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 200.00}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("OK");
    }

    @Test
    void testMakePayment_insufficientFunds() {
        webTestClient.post()
                .uri("/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"amount\": 2000.00}")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.status").isEqualTo("FAILED");
    }

    @Test
    void testGetBalance_success() {
        webTestClient.get()
                .uri("/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.amount").isEqualTo(800.00);
    }
}