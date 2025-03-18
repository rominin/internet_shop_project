package ru.practicum.java.internet_shop_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.practicum.java.internet_shop_project.entity.Cart;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class CartWithItemsDto {

    private Long id;
    private List<CartItemDto> cartItems;
    private BigDecimal totalPrice;

    public CartWithItemsDto(Cart cart, List<CartItemDto> cartItems) {
        this.id = cart.getId();
        this.cartItems = cartItems;
        this.totalPrice = cart.getTotalPrice();
    }

    public boolean isEmpty() {
        return cartItems == null || cartItems.isEmpty();
    }
}
