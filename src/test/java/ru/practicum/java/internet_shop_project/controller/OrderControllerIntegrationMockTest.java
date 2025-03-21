package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.http.MediaType;
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

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebFluxTest(OrderController.class)
public class OrderControllerIntegrationMockTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetAllOrders_success() {
        List<OrderWithItemsDto> orders = List.of(
                new OrderWithItemsDto(1L, List.of(), BigDecimal.valueOf(1500)),
                new OrderWithItemsDto(2L, List.of(), BigDecimal.valueOf(2500))
        );

        when(orderService.getAllOrders()).thenReturn(Flux.fromIterable(orders));
        when(orderService.getTotalOrdersPrice()).thenReturn(Mono.just(BigDecimal.valueOf(4000)));

        webTestClient.get()
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
                List.of(new OrderItemDto(1L, 1L, 1,new Product(1L, "Laptop", "", "", BigDecimal.valueOf(1500)))),
                BigDecimal.valueOf(1500)
        );

        when(orderService.getOrderById(1L)).thenReturn(Mono.just(orderDto));

        webTestClient.get()
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
        when(orderService.createOrderFromCart()).thenReturn(Mono.just(order));

        webTestClient.post()
                .uri("/orders/checkout")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().valueEquals("Location", "/orders/1");

        verify(orderService, times(1)).createOrderFromCart();
    }

}
