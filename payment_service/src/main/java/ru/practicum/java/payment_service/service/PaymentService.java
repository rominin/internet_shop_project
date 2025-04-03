package ru.practicum.java.payment_service.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.java.payment_service.config.PaymentProperties;

import java.math.BigDecimal;
import java.util.concurrent.atomic.AtomicReference;

@Service
public class PaymentService {

    private final AtomicReference<BigDecimal> balance;

    public PaymentService(PaymentProperties paymentProperties) {
        this.balance = new AtomicReference<>(paymentProperties.getInitialBalance());
    }

    public Mono<BigDecimal> getBalance() {
        return Mono.just(balance.get());
    }

    public Mono<Boolean> pay(BigDecimal amount) {
        return Mono.fromSupplier(() -> {
                    BigDecimal currentBalance = balance.get();
                    if (currentBalance.compareTo(amount) >= 0) {
                        balance.set(currentBalance.subtract(amount));
                        return true;
                    } else {
                        return false;
                    }
                }
        );
    }

}
