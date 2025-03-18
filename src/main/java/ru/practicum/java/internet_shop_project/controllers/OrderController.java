package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.service.OrderService;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public Mono<Rendering> getAllOrders() {
        return orderService.getAllOrders()
                .collectList()
                .zipWith(orderService.getTotalOrdersPrice())
                .map(tuple -> Rendering.view("orders")
                        .modelAttribute("orders", tuple.getT1())
                        .modelAttribute("totalOrdersPrice", tuple.getT2())
                        .build()
                );
    }

    @GetMapping("/{orderId}")
    public Mono<Rendering> getOrderById(@PathVariable Long orderId) {
        return orderService.getOrderById(orderId)
                .map(order -> Rendering.view("order")
                        .modelAttribute("order", order)
                        .build()
                );
    }

    @PostMapping("/checkout")
    public Mono<Rendering> checkoutOrder() {
        return orderService.createOrderFromCart()
                .map(order -> Rendering.view("redirect:/orders/" + order.getId()).build());
    }

}
