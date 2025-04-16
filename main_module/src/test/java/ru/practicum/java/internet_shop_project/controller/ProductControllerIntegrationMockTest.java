package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.controllers.ProductController;
import ru.practicum.java.internet_shop_project.dto.ProductListItemDto;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.ProductService;
import ru.practicum.java.internet_shop_project.service.ViewAccessHelper;

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

    @MockitoBean
    private ViewAccessHelper viewAccessHelper;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetProducts_success() {
        List<ProductListItemDto> products = List.of(
                new ProductListItemDto(1L, "Laptop", "Some Laptop", new BigDecimal("1500.00"), "someUrl"),
                new ProductListItemDto(2L, "Phone", "Some Smartphone", new BigDecimal("800.00"), "someUrl")
        );

        when(productService.getFilteredAndSortedProductsWithCaching(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(Flux.fromIterable(products));

        webTestClient = webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password", List.of())
        ));

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

        webTestClient = webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(
                new UsernamePasswordAuthenticationToken("user", "password", List.of())
        ));

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

}