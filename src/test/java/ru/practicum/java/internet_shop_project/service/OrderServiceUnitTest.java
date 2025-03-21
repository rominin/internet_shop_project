package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.dto.OrderItemDto;
import ru.practicum.java.internet_shop_project.entity.*;
import ru.practicum.java.internet_shop_project.mappers.OrderItemMapper;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.OrderItemRepository;
import ru.practicum.java.internet_shop_project.repository.OrderRepository;

import java.math.BigDecimal;
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

    @MockitoBean
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderService orderService;

    private Cart cart;
    private Order order;
    private OrderItem orderItem;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        cart = new Cart(1L, BigDecimal.valueOf(300));
        order = new Order(1L, BigDecimal.valueOf(300));
        cartItem = new CartItem(1L, 1L, 1L, 2);
        orderItem = new OrderItem(1L, 1L, 1L, 2);
    }

    @Test
    void testGetAllOrders_success() {
        Order order1 = new Order(1L, BigDecimal.valueOf(500));
        Order order2 = new Order(2L, BigDecimal.valueOf(700));

        OrderItem orderItem1 = new OrderItem(1L, 1L, 101L, 2);
        OrderItem orderItem2 = new OrderItem(2L, 2L, 102L, 3);

        OrderItemDto orderItemDto1 = new OrderItemDto(1L, 1L, 2, null);
        OrderItemDto orderItemDto2 = new OrderItemDto(2L, 2L, 3, null);

        when(orderRepository.findAll()).thenReturn(Flux.just(order1, order2));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem1));
        when(orderItemRepository.findByOrderId(2L)).thenReturn(Flux.just(orderItem2));

        when(orderItemMapper.toDtoList(any(Flux.class))).thenAnswer(invocation -> {
            Flux<OrderItem> orderItemsFlux = invocation.getArgument(0);
            return orderItemsFlux.flatMap(item -> {
                if (item.getOrderId() == 1L) return Mono.just(orderItemDto1);
                else return Mono.just(orderItemDto2);
            });
        });

        StepVerifier.create(orderService.getAllOrders())
                .assertNext(orderDto -> {
                    assert orderDto != null;
                    assert orderDto.getId().equals(1L);
                    assert orderDto.getTotalPrice().equals(BigDecimal.valueOf(500));
                    assert orderDto.getOrderItems().size() == 1;
                })
                .assertNext(orderDto -> {
                    assert orderDto != null;
                    assert orderDto.getId().equals(2L);
                    assert orderDto.getTotalPrice().equals(BigDecimal.valueOf(700));
                    assert orderDto.getOrderItems().size() == 1;
                })
                .verifyComplete();
    }

    @Test
    void testGetOrderById_success() {
        Order order = new Order(1L, BigDecimal.valueOf(500));

        OrderItem orderItem = new OrderItem(1L, 1L, 101L, 2);
        OrderItemDto orderItemDto = new OrderItemDto(1L, 1L, 2, null);

        when(orderRepository.findById(1L)).thenReturn(Mono.just(order));
        when(orderItemRepository.findByOrderId(1L)).thenReturn(Flux.just(orderItem));

        when(orderItemMapper.toDtoList(any(Flux.class))).thenReturn(Flux.just(orderItemDto));

        StepVerifier.create(orderService.getOrderById(1L))
                .assertNext(orderDtoResult -> {
                    assert orderDtoResult != null;
                    assert orderDtoResult.getId().equals(1L);
                    assert orderDtoResult.getTotalPrice().equals(BigDecimal.valueOf(500));
                    assert orderDtoResult.getOrderItems().size() == 1;
                })
                .verifyComplete();
    }

    @Test
    void testGetOrderById_failure() {
        when(orderRepository.findById(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.getOrderById(1L))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Order not found"))
                .verify();

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void testCreateOrderFromCart_success() {
        when(cartRepository.findById(1L)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(1L)).thenReturn(Flux.just(cartItem));
        when(orderRepository.save(any(Order.class))).thenReturn(Mono.just(order));
        when(orderItemRepository.save(any(OrderItem.class))).thenReturn(Mono.just(orderItem));
        when(cartItemRepository.clearCartItems(1L)).thenReturn(Mono.empty());

        StepVerifier.create(orderService.createOrderFromCart())
                .expectNextMatches(o -> o.getTotalPrice().equals(BigDecimal.valueOf(300)))
                .verifyComplete();

        verify(orderRepository, times(1)).save(any(Order.class));
        verify(orderItemRepository, times(1)).save(any(OrderItem.class));
        verify(cartItemRepository, times(1)).clearCartItems(1L);
    }

    @Test
    void testCreateOrderFromCart_failure_emptyCart() {
        Cart emptyCart = new Cart(1L, BigDecimal.ZERO);
        when(cartRepository.findById(1L)).thenReturn(Mono.just(emptyCart));

        StepVerifier.create(orderService.createOrderFromCart())
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Cart is empty"))
                .verify();

        verifyNoInteractions(orderRepository, orderItemRepository, cartItemRepository);
    }

    @Test
    void testGetTotalOrdersPrice_success() {
        when(orderRepository.findAll()).thenReturn(Flux.just(
                new Order(1L, BigDecimal.valueOf(500)),
                new Order(2L, BigDecimal.valueOf(700))
        ));

        StepVerifier.create(orderService.getTotalOrdersPrice())
                .expectNext(BigDecimal.valueOf(1200))
                .verifyComplete();

        verify(orderRepository, times(1)).findAll();
    }

}
