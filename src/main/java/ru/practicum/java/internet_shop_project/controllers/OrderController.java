package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.service.OrderService;

import java.util.List;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @GetMapping
    public String getAllOrders(Model model) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        model.addAttribute("totalOrdersPrice", orderService.getTotalOrdersPrice());
        return "orders";
    }

    @GetMapping("/{orderId}")
    public String getOrderById(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        model.addAttribute("order", order);
        return "order";
    }

    @PostMapping("/checkout")
    public String checkoutOrder() {
        Order order = orderService.createOrderFromCart();
        return "redirect:/orders/" + order.getId();
    }

}
