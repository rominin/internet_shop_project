package ru.practicum.java.payment_service.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.java.payment_service.config.PaymentPropertiesConfig;

import java.math.BigDecimal;

@SpringBootTest(classes = PaymentService.class)
@ActiveProfiles("test")
@Import(PaymentPropertiesConfig.class)
public class PaymentServiceUnitTest {

    @Autowired
    private PaymentService paymentService;

    @Test
    void pay_shouldSucceed_whenEnoughBalance() {
        paymentService.pay(new BigDecimal("400.00"))
                .as(StepVerifier::create)
                .expectNext(true)
                .verifyComplete();

        paymentService.getBalance()
                .as(StepVerifier::create)
                .expectNext(new BigDecimal("600.00"))
                .verifyComplete();
    }

    @Test
    void pay_shouldFail_whenNotEnoughBalance() {
        paymentService.pay(new BigDecimal("1500.00"))
                .as(StepVerifier::create)
                .expectNext(false)
                .verifyComplete();

        paymentService.getBalance()
                .as(StepVerifier::create)
                .expectNext(new BigDecimal("1000.00"))
                .verifyComplete();
    }

    @Test
    void testGetBalance_success() {
        paymentService.getBalance()
                .as(StepVerifier::create)
                .expectNext(new BigDecimal("600.00"))
                .verifyComplete();
    }

}
