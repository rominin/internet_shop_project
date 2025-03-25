package ru.practicum.java.internet_shop_project.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.dto.OrderItemDto;
import ru.practicum.java.internet_shop_project.entity.OrderItem;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

@Component
@RequiredArgsConstructor
public class OrderItemMapper {

    private final ProductRepository productRepository;

    public Mono<OrderItemDto> toDto(OrderItem orderItem) {
        return productRepository.findById(orderItem.getProductId())
                .map(product -> new OrderItemDto(
                        orderItem.getId(),
                        orderItem.getOrderId(),
                        orderItem.getQuantity(),
                        product
                ))
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found for order item")));
    }

    public Flux<OrderItemDto> toDtoList(Flux<OrderItem> orderItems) {
        return orderItems.flatMap(this::toDto);
    }
}
