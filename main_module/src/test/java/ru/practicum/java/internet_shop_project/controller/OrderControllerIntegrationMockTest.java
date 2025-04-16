package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.controllers.OrderController;
import ru.practicum.java.internet_shop_project.dto.OrderItemDto;
import ru.practicum.java.internet_shop_project.dto.OrderWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.OrderService;
import ru.practicum.java.internet_shop_project.service.UserContextService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebFluxTest(OrderController.class)
public class OrderControllerIntegrationMockTest {

    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private UserContextService userContextService;

    @Autowired
    private WebTestClient webTestClient;

    private final OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
            new DefaultOAuth2User(
                    List.of(new SimpleGrantedAuthority("ROLE_USER")),
                    Map.of("preferred_username", "testuser"),
                    "preferred_username"
            ),
            List.of(new SimpleGrantedAuthority("ROLE_USER")),
            "keycloak"
    );


    @Test
    void testGetAllOrders_success() {
        List<OrderWithItemsDto> orders = List.of(
                new OrderWithItemsDto(1L, List.of(), BigDecimal.valueOf(1500)),
                new OrderWithItemsDto(2L, List.of(), BigDecimal.valueOf(2500))
        );

        when(userContextService.getCurrentUserId(authentication)).thenReturn(Mono.just(1L));
        when(orderService.getAllOrders(1L)).thenReturn(Flux.fromIterable(orders));
        when(orderService.getTotalOrdersPrice(1L)).thenReturn(Mono.just(BigDecimal.valueOf(4000)));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody)
                            .isNotNull()
                            .contains("1500", "2500", "4000")
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testGetOrderById_success() {
        OrderWithItemsDto orderDto = new OrderWithItemsDto(
                1L,
                List.of(new OrderItemDto(1L, 1L, 1,
                        new Product(1L, "Laptop", "", "", BigDecimal.valueOf(1500)))),
                BigDecimal.valueOf(1500)
        );

        when(userContextService.getCurrentUserId(authentication)).thenReturn(Mono.just(1L));
        when(orderService.getOrderById(1L, 1L)).thenReturn(Mono.just(orderDto));

        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
                .uri("/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("text/html")
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody)
                            .isNotNull()
                            .contains("Laptop")
                            .contains("1500")
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testCheckoutOrder_success() {
        Order order = Order.builder().id(1L).totalPrice(BigDecimal.valueOf(1500)).build();

        when(userContextService.getCurrentUserId(authentication)).thenReturn(Mono.just(1L));
        when(orderService.createOrderFromCart(1L)).thenReturn(Mono.just(order));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .mutateWith(SecurityMockServerConfigurers.csrf())
                .post()
                .uri("/orders/checkout")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().valueEquals("Location", "/orders/1");

        verify(orderService, times(1)).createOrderFromCart(1L);
    }

}
