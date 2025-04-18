package ru.practicum.java.internet_shop_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.java.internet_shop_project.entity.Product;

@Data
@AllArgsConstructor
public class CartItemDto {
    private Long id;
    private Long cartId;
    private Integer quantity;
    private Product product;
}
