package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClientV2;
import ru.practicum.java.internet_shop_project.controllers.CartController;
import ru.practicum.java.internet_shop_project.dto.CartItemDto;
import ru.practicum.java.internet_shop_project.dto.CartWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.CartService;
import ru.practicum.java.internet_shop_project.service.UserContextService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@WebFluxTest(CartController.class)
public class CartControllerIntegrationMockTest {

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private PaymentClientV2 paymentClient;

    @MockitoBean
    private UserContextService userContextService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetCart_success() {
        Cart cart = new Cart(15L, 1L, BigDecimal.valueOf(1000));
        CartItemDto cartItem = new CartItemDto(1L, 1L, 2, new Product(1L, "Laptop", "", "", BigDecimal.valueOf(500)));
        CartWithItemsDto cartDto = new CartWithItemsDto(cart, List.of(cartItem));

        when(cartService.getCart(1L)).thenReturn(Mono.just(cartDto));
        when(paymentClient.getBalance(1L)).thenReturn(Mono.just(BigDecimal.valueOf(1000)));
        when(userContextService.getCurrentUserId(any(Authentication.class))).thenReturn(Mono.just(1L));

        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("preferred_username", "user1"),
                "preferred_username"
        );
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(oauthUser, oauthUser.getAuthorities(), "keycloak");

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
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
