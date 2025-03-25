package ru.practicum.java.internet_shop_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.mappers.OrderItemMapper;
import ru.practicum.java.internet_shop_project.dto.OrderWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.OrderItem;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.OrderItemRepository;
import ru.practicum.java.internet_shop_project.repository.OrderRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private static final Long SINGLETON_CARD_ID = 1L;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemMapper orderItemMapper;

    public Flux<OrderWithItemsDto> getAllOrders() {
        return orderRepository.findAll()
                .flatMap(order ->
                        orderItemMapper.toDtoList(orderItemRepository.findByOrderId(order.getId()))
                                .collectList()
                                .map(items -> new OrderWithItemsDto(order, items))
                );
    }

    public Mono<OrderWithItemsDto> getOrderById(Long orderId) {
        return orderRepository.findById(orderId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order ->
                        orderItemMapper.toDtoList(orderItemRepository.findByOrderId(order.getId()))
                                .collectList()
                                .map(items -> new OrderWithItemsDto(order, items))
                );
    }

    public Mono<Order> createOrderFromCart() {
        return cartRepository.findById(SINGLETON_CARD_ID)
                .switchIfEmpty(Mono.error(new RuntimeException("Cart not found")))
                .flatMap(cart -> {
                    if (cart.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return Mono.error(new RuntimeException("Cart is empty"));
                    }

                    Order order = Order.builder()
                            .totalPrice(cart.getTotalPrice())
                            .build();

                    return orderRepository.save(order)
                            .flatMap(savedOrder -> cartItemRepository.findByCartId(SINGLETON_CARD_ID)
                                    .flatMap(cartItem -> {
                                        OrderItem orderItem = new OrderItem(null, savedOrder.getId(), cartItem.getProductId(), cartItem.getQuantity());
                                        return orderItemRepository.save(orderItem);
                                    })
                                    .then(cartItemRepository.clearCartItems(SINGLETON_CARD_ID))
                                    .thenReturn(savedOrder));
                });
    }

    public Mono<BigDecimal> getTotalOrdersPrice() {
        return orderRepository.findAll()
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
