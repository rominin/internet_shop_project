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
import ru.practicum.java.internet_shop_project.dto.OrderItemDto;
import ru.practicum.java.internet_shop_project.dto.OrderWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test-webflux2")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OrderControllerIntegrationTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WebTestClient webTestClient;

    private Order savedOrder;

    @BeforeEach
    void setUp() {
        Product product = new Product(null, "Headphones", "headphones.jpg", "Wireless noise-canceling headphones", new BigDecimal("200"));
        OrderItemDto orderItem = new OrderItemDto(1L, 1L, 2, product);

        OrderWithItemsDto orderDto = new OrderWithItemsDto(
                1L, List.of(orderItem), BigDecimal.valueOf(400)
        );

        savedOrder = Order.builder()
                .totalPrice(orderDto.getTotalPrice())
                .build();

        savedOrder = orderRepository.save(savedOrder).block();
    }

    @Test
    void testGetAllOrders_success() {
        webTestClient.get()
                .uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull()
                            .contains("400")
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testGetOrderById_success() {
        webTestClient.get()
                .uri("/orders/" + savedOrder.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull()
                            .contains("<html>", "<body>");
                });
    }

}
