package ru.practicum.java.payment_service.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@TestConfiguration
public class PaymentPropertiesConfig {

    @Bean
    public PaymentProperties paymentProperties() {
        PaymentProperties props = new PaymentProperties();
        props.setInitialBalance(new BigDecimal("1000.00"));
        return props;
    }

}
