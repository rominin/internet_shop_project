package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.OrderItemRepository;
import ru.practicum.java.internet_shop_project.repository.OrderRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = OrderService.class)
public class OrderServiceUnitTest {

    @MockitoBean
    private OrderRepository orderRepository;

    @MockitoBean
    private OrderItemRepository orderItemRepository;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderService orderService;

    private Cart cart;
    private Product product1;
    private Product product2;
    private CartItem cartItem1;
    private CartItem cartItem2;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cart.setTotalPrice(BigDecimal.valueOf(300));

        product1 = new Product();
        product1.setId(1L);
        product1.setPrice(BigDecimal.valueOf(100));

        product2 = new Product();
        product2.setId(2L);
        product2.setPrice(BigDecimal.valueOf(200));

        cartItem1 = new CartItem(1L, cart, product1, 1);
        cartItem2 = new CartItem(2L, cart, product2, 1);

        cart.setCartItems(List.of(cartItem1, cartItem2));
    }

    @Test
    void testGetAllOrders_success() {
        List<Order> orders = List.of(
                Order.builder().id(1L).totalPrice(BigDecimal.valueOf(500)).build(),
                Order.builder().id(2L).totalPrice(BigDecimal.valueOf(700)).build()
        );

        when(orderRepository.findAll()).thenReturn(orders);

        List<Order> result = orderService.getAllOrders();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(BigDecimal.valueOf(500), result.getFirst().getTotalPrice());
        assertEquals(BigDecimal.valueOf(700), result.getLast().getTotalPrice());

        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void testGetOrderById_success() {
        Order order = Order.builder().id(1L).totalPrice(BigDecimal.valueOf(500)).build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order result = orderService.getOrderById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(BigDecimal.valueOf(500), result.getTotalPrice());

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testGetOrderById_failure() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> orderService.getOrderById(1L));

        assertEquals("Order not found", exception.getMessage());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateOrderFromCart_success() {
        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order savedOrder = invocation.getArgument(0);
            savedOrder.setId(1L);
            return savedOrder;
        });

        Order createdOrder = orderService.createOrderFromCart();

        assertNotNull(createdOrder);
        assertEquals(BigDecimal.valueOf(300), createdOrder.getTotalPrice());

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).saveAll(anyList());
        verify(cartItemRepository, times(1)).clearCartItemsInSingletonCart();
    }

    @Test
    void testCreateOrderFromCart_failure() {
        cart.setCartItems(List.of());
        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));

        Exception exception = assertThrows(RuntimeException.class, () -> orderService.createOrderFromCart());

        assertEquals("Cart is empty", exception.getMessage());
        verifyNoInteractions(orderRepository, orderItemRepository, cartItemRepository);
    }

    @Test
    void testGetTotalOrdersPrice_success() {
        List<Order> orders = List.of(
                Order.builder().id(1L).totalPrice(BigDecimal.valueOf(500)).build(),
                Order.builder().id(2L).totalPrice(BigDecimal.valueOf(700)).build()
        );

        when(orderRepository.findAll()).thenReturn(orders);

        BigDecimal totalOrdersPrice = orderService.getTotalOrdersPrice();

        assertEquals(BigDecimal.valueOf(1200), totalOrdersPrice);
        verify(orderRepository, times(1)).findAll();
    }


}
