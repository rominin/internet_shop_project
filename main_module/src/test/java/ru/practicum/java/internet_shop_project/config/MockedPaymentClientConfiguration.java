package ru.practicum.java.internet_shop_project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClient;
import ru.practicum.java.paymentclient.api.DefaultApi;
import ru.practicum.java.paymentclient.model.BalanceResponse;
import ru.practicum.java.paymentclient.model.PaymentRequest;
import ru.practicum.java.paymentclient.model.PaymentResponse;

import java.math.BigDecimal;

@TestConfiguration
public class MockedPaymentClientConfiguration {

    @Bean
    public PaymentClient paymentClient() {
        return new PaymentClient(new DefaultApi() {
            @Override
            public Mono<BalanceResponse> getBalance() {
                BalanceResponse response =  new BalanceResponse();
                response.setAmount(new BigDecimal("9999999.99"));
                return Mono.just(response);
            }

            @Override
            public Mono<PaymentResponse> makePayment(PaymentRequest paymentRequest) {
                PaymentResponse response =  new PaymentResponse();
                response.setStatus(PaymentResponse.StatusEnum.OK);
                return Mono.just(response);
            }
        });
    }

}
