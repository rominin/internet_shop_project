package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    private OAuth2AuthenticationToken authentication;

    @BeforeEach
    void setUp() {
        DefaultOAuth2User oauthUser = new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of("preferred_username", "testuser"),
                "preferred_username"
        );
        authentication = new OAuth2AuthenticationToken(oauthUser, oauthUser.getAuthorities(), "keycloak");

        savedOrder = orderRepository.save(
                Order.builder()
                        .userId(1L)
                        .totalPrice(BigDecimal.valueOf(400))
                        .build()
        ).block();
    }

    @Test
    void testGetAllOrders_success() {
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
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
        webTestClient.mutateWith(SecurityMockServerConfigurers.mockAuthentication(authentication))
                .get()
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
