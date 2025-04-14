package ru.practicum.java.payment_service.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "user_balances")
public class UserBalance {

    @Id
    private Long id;
    private Long userId;
    private BigDecimal balance;

    public UserBalance() {
    }

    public UserBalance(Long userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
