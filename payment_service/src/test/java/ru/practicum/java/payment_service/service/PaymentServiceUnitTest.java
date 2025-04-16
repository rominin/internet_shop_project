package ru.practicum.java.payment_service.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.payment_service.config.PaymentProperties;
import ru.practicum.java.payment_service.config.PaymentPropertiesConfig;
import ru.practicum.java.payment_service.entity.UserBalance;
import ru.practicum.java.payment_service.repository.UserBalanceRepository;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = PaymentService.class)
@ActiveProfiles("test")
@Import(PaymentPropertiesConfig.class)
public class PaymentServiceUnitTest {

    @Autowired
    private PaymentService paymentService;

    @MockitoBean
    private UserBalanceRepository userBalanceRepository;

    @MockitoBean
    private PaymentProperties paymentProperties;

    private final Long userId = 42L;
    private final BigDecimal initialBalance = new BigDecimal("1000.00");

    @BeforeEach
    void setUp() {
        when(paymentProperties.getInitialBalance()).thenReturn(initialBalance);
    }

    @Test
    void pay_shouldSucceed_whenEnoughBalance() {
        UserBalance existingBalance = new UserBalance(userId, initialBalance);

        when(userBalanceRepository.findByUserId(userId)).thenReturn(Mono.just(existingBalance));
        when(userBalanceRepository.save(any(UserBalance.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(paymentService.pay(userId, new BigDecimal("400.00")))
                .expectNext(true)
                .verifyComplete();

        verify(userBalanceRepository).save(argThat(balance ->
                balance.getBalance().compareTo(new BigDecimal("600.00")) == 0
        ));
    }

    @Test
    void getBalance_shouldCreateNew_whenNotFound() {
        when(userBalanceRepository.findByUserId(userId)).thenReturn(Mono.empty());
        when(userBalanceRepository.save(any(UserBalance.class)))
                .thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(paymentService.getBalance(userId))
                .expectNext(initialBalance)
                .verifyComplete();

        verify(userBalanceRepository).save(argThat(balance -> balance.getUserId().equals(userId)));
    }

}
