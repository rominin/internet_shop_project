package ru.practicum.java.internet_shop_project.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

    @Id
    private Long id;

    private Long cartId;

    private Long productId;

    private Integer quantity;

}