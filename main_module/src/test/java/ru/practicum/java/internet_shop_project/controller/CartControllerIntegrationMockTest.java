package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClient;
import ru.practicum.java.internet_shop_project.controllers.CartController;
import ru.practicum.java.internet_shop_project.dto.CartItemDto;
import ru.practicum.java.internet_shop_project.dto.CartWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.CartService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
public class CartControllerIntegrationMockTest {

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private PaymentClient paymentClient;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetCart_success() {
        Cart cart = new Cart(1L, BigDecimal.valueOf(1000));
        CartItemDto cartItem = new CartItemDto(1L, 1L, 2, new Product(1L, "Laptop", "", "", BigDecimal.valueOf(500)));
        CartWithItemsDto cartDto = new CartWithItemsDto(cart, List.of(cartItem));

        when(cartService.getCart()).thenReturn(Mono.just(cartDto));
        when(paymentClient.getBalance()).thenReturn(Mono.just(BigDecimal.valueOf(1000)));

        webTestClient.get()
                .uri("/cart")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assert responseBody != null;
                    assert responseBody.contains("<html>") && responseBody.contains("<body>");
                    assert responseBody.contains("Laptop") && responseBody.contains("1000");
                });
    }

}
