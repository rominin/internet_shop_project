package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test-webflux3")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class CartControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product product;
    private Cart cart;

    @BeforeAll
    static void beforeAll(@Autowired CartRepository cartRepository) {
        cartRepository.save(new Cart()).block();
    }

    @BeforeEach
    void setUp() {
        cart = cartRepository.findById(1L)
                .blockOptional()
                .orElseGet(() -> cartRepository.save(new Cart()).block());

        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart).block();

        product = productRepository.save(
                new Product(null, "Wireless Mouse", "mouse.jpg", "Ergonomic wireless mouse", new BigDecimal("40.00"))
        ).block();
    }

    @Test
    void testGetCart_success() {
        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull()
                            .contains("<html>", "<body>")
                            .contains("Корзина");
                });
    }

}
