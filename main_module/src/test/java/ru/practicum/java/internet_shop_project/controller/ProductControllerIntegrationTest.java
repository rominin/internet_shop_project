package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test-webflux")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        List<Product> products = List.of(
                new Product(null, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")),
                new Product(null, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00"))
        );
        productRepository.saveAll(products).collectList().block();
    }

    @Test
    void testGetProducts_returnsProductList() {
        webTestClient.get()
                .uri("/products")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertThat(body).isNotNull()
                            .contains("Laptop")
                            .contains("Phone")
                            .contains("<html>");
                });
    }

    @Test
    void testGetProductById_returnsSingleProduct() {
        Long productId = productRepository.findAll()
                .filter(p -> p.getName().equals("Laptop"))
                .map(Product::getId)
                .blockFirst();

        webTestClient.get()
                .uri("/products/" + productId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String body = response.getResponseBody();
                    assertThat(body).isNotNull()
                            .contains("Laptop")
                            .contains("Some Laptop")
                            .contains("<html>");
                });
    }

}
