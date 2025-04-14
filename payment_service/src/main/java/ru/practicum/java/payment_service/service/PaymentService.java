package ru.practicum.java.payment_service.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.java.payment_service.config.PaymentProperties;
import ru.practicum.java.payment_service.entity.UserBalance;
import ru.practicum.java.payment_service.repository.UserBalanceRepository;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final UserBalanceRepository userBalanceRepository;
    private final PaymentProperties paymentProperties;

    public PaymentService(PaymentProperties paymentProperties, UserBalanceRepository userBalanceRepository) {
        this.userBalanceRepository = userBalanceRepository;
        this.paymentProperties = paymentProperties;
    }

    public Mono<BigDecimal> getBalance(Long userId) {
        return userBalanceRepository.findByUserId(userId)
                .switchIfEmpty(userBalanceRepository.save(new UserBalance(userId, paymentProperties.getInitialBalance())))
                .map(UserBalance::getBalance);
    }

    public Mono<Boolean> pay(Long userId, BigDecimal amount) {
        return userBalanceRepository.findByUserId(userId)
                .switchIfEmpty(userBalanceRepository.save(new UserBalance(userId, paymentProperties.getInitialBalance())))
                .flatMap(userBalance -> {
                    BigDecimal balance = userBalance.getBalance();
                    if (balance.compareTo(amount) >= 0) {
                        userBalance.setBalance(balance.subtract(amount));
                        return userBalanceRepository.save(userBalance).thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

}
