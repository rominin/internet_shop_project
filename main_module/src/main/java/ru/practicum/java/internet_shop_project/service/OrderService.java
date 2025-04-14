package ru.practicum.java.internet_shop_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClient;
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

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final PaymentClient paymentClient;

    public Flux<OrderWithItemsDto> getAllOrders(Long userId) {
        return orderRepository.findAllByUserId(userId)
                .flatMap(order ->
                        orderItemMapper.toDtoList(orderItemRepository.findByOrderId(order.getId()))
                                .collectList()
                                .map(items -> new OrderWithItemsDto(order, items))
                );
    }

    public Mono<OrderWithItemsDto> getOrderById(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Order not found")))
                .flatMap(order ->
                        orderItemMapper.toDtoList(orderItemRepository.findByOrderId(order.getId()))
                                .collectList()
                                .map(items -> new OrderWithItemsDto(order, items))
                );
    }

    public Mono<Order> createOrderFromCart(Long userId) {
        return cartRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(new RuntimeException("Cart not found")))
                .flatMap(cart -> {
                    if (cart.getTotalPrice().compareTo(BigDecimal.ZERO) == 0) {
                        return Mono.error(new RuntimeException("Cart is empty"));
                    }

                    return paymentClient.makePayment(cart.getTotalPrice())
                            .flatMap(success -> {
                                if (!success) {
                                    return Mono.error(new RuntimeException("Payment failed: not enough funds. Revisit cart (url should be clear: \"/cart\")"));
                                }

                                Order order = Order.builder()
                                        .userId(userId)
                                        .totalPrice(cart.getTotalPrice())
                                        .build();

                                return orderRepository.save(order)
                                        .flatMap(savedOrder -> cartItemRepository.findByCartId(cart.getId())
                                                .flatMap(cartItem -> {
                                                    OrderItem orderItem = new OrderItem(null, savedOrder.getId(), cartItem.getProductId(), cartItem.getQuantity());
                                                    return orderItemRepository.save(orderItem);
                                                })
                                                .then(cartItemRepository.clearCartItems(cart.getId()))
                                                .thenReturn(savedOrder));
                            });
                });
    }

    public Mono<BigDecimal> getTotalOrdersPrice(Long userId) {
        return orderRepository.findAllByUserId(userId)
                .map(Order::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}
