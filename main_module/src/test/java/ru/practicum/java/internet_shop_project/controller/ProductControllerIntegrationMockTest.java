package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.controllers.ProductController;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@WebFluxTest(controllers = ProductController.class)
public class ProductControllerIntegrationMockTest {

    @MockitoBean
    private ProductService productService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetProducts_success() {
        List<Product> products = List.of(
                new Product(1L, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")),
                new Product(2L, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00"))
        );

        when(productService.getFilteredAndSortedProducts(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(Flux.fromIterable(products));

        webTestClient.get()
                .uri("/products?page=0&size=10&sortBy=name&sortOrder=asc")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody)
                            .isNotNull()
                            .contains("Laptop", "Phone")
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testGetProductById_success() {
        Product product = new Product(1L, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00"));

        when(productService.getProductById(anyLong())).thenReturn(Mono.just(product));

        webTestClient.get()
                .uri("/products/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull()
                            .contains("Laptop")
                            .contains("1500.00")
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testShowImportPage_success() {
        webTestClient.get()
                .uri("/products/import")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("<title>Импорт товаров</title>"));
    }

}