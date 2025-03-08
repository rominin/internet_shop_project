package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.java.internet_shop_project.controllers.OrderController;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.service.OrderService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerIntegrationMockTest {

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetAllOrders_success() throws Exception {
        List<Order> orders = List.of(
                Order.builder().id(1L).totalPrice(BigDecimal.valueOf(1500)).build(),
                Order.builder().id(2L).totalPrice(BigDecimal.valueOf(2500)).build()
        );
        when(orderService.getAllOrders()).thenReturn(orders);
        when(orderService.getTotalOrdersPrice()).thenReturn(BigDecimal.valueOf(4000));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(model().attribute("orders", orders))
                .andExpect(model().attribute("totalOrdersPrice", BigDecimal.valueOf(4000)));
    }

    @Test
    void testGetOrderById_success() throws Exception {
        Order order = Order.builder()
                .id(1L)
                .totalPrice(BigDecimal.valueOf(1500))
                .orderItems(List.of())
                .build();
        when(orderService.getOrderById(1L)).thenReturn(order);

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attribute("order", order));
    }

    @Test
    void testCheckoutOrder_success() throws Exception {
        Order order = Order.builder().id(1L).totalPrice(BigDecimal.valueOf(1500)).build();
        when(orderService.createOrderFromCart()).thenReturn(order);

        mockMvc.perform(post("/orders/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/orders/1"));

        verify(orderService, times(1)).createOrderFromCart();
    }

}
