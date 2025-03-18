package ru.practicum.java.internet_shop_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.java.internet_shop_project.entity.Order;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class OrderWithItemsDto {

    private Long id;
    private List<OrderItemDto> orderItems;
    private BigDecimal totalPrice;

    public OrderWithItemsDto(Order order, List<OrderItemDto> orderItems) {
        this.id = order.getId();
        this.orderItems = orderItems;
        this.totalPrice = order.getTotalPrice();
    }

    public boolean isEmpty() {
        return orderItems == null || orderItems.isEmpty();
    }
}
