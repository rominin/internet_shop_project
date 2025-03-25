package ru.practicum.java.internet_shop_project.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {

    @Id
    private Long id;

    private Long orderId;

    private Long productId;

    private Integer quantity;

}