package ru.practicum.java.internet_shop_project.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "carts")
@Getter
@Setter
public class Cart {

    @Id
    private Long id;

    private Long userId;

    private BigDecimal totalPrice;

    public Cart() {
        this.totalPrice = BigDecimal.ZERO;
    }

    public Cart(Long userId) {
        this.userId = userId;
        this.totalPrice = BigDecimal.ZERO;
    }

    public Cart(Long id, Long userId, BigDecimal totalPrice) {
        this.id = id;
        this.userId = userId;
        this.totalPrice = totalPrice != null ? totalPrice : BigDecimal.ZERO;
    }

}