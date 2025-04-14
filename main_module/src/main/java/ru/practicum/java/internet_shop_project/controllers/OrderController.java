package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.util.UriUtils;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.service.OrderService;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> getAllOrders() {
        return orderService.getAllOrders(1L)// TODO real Id
                .collectList()
                .zipWith(orderService.getTotalOrdersPrice(1L))// TODO real Id
                .map(tuple -> Rendering.view("orders")
                        .modelAttribute("orders", tuple.getT1())
                        .modelAttribute("totalOrdersPrice", tuple.getT2())
                        .build()
                );
    }

    @GetMapping("/{orderId}")
    public Mono<Rendering> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId, 1L)// TODO real Id
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .build()
                );
    }

    @PostMapping("/checkout")
    public Mono<Rendering> checkoutOrder() {
        return orderService.createOrderFromCart(1L)// TODO real Id
                .map(order -> Rendering.view("redirect:/orders/" + order.getId()).build())
                .onErrorResume(e -> {
                    String message = e instanceof WebClientRequestException
                            ? "Сервис платежей временно недоступен. Попробуйте вернуться в корзину позже"
                            : (e.getMessage() != null ? e.getMessage() : "Ошибка при оформлении заказа");

                    String encodedMessage = UriUtils.encode(message, StandardCharsets.UTF_8);
                    return Mono.just(Rendering.redirectTo("/cart?errorMessage=" + encodedMessage).build());
                });
    }

}
