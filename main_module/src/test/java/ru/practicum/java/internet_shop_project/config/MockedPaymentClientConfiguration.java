package ru.practicum.java.internet_shop_project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClientV2;
import ru.practicum.java.paymentclient.model.BalanceResponse;
import ru.practicum.java.paymentclient.model.PaymentResponse;

import java.math.BigDecimal;
import java.util.function.Function;

@TestConfiguration
public class MockedPaymentClientConfiguration {

    @Bean
    public PaymentClientV2 paymentClient() {
        ExchangeFunction mockExchange = request -> {
            String path = request.url().getPath();

            if (path.equals("/balance")) {
                BalanceResponse balance = new BalanceResponse();
                balance.setAmount(new BigDecimal("9999999.99"));

                return Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .body((Function<Flux<DataBuffer>, Flux<DataBuffer>>) BodyInserters.fromValue(balance))
                                .build()
                );
            }

            if (path.equals("/pay")) {
                PaymentResponse payment = new PaymentResponse();
                payment.setStatus(PaymentResponse.StatusEnum.OK);

                return Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .body((Function<Flux<DataBuffer>, Flux<DataBuffer>>) BodyInserters.fromValue(payment))
                                .build()
                );
            }

            return Mono.error(new IllegalArgumentException("Unexpected request path: " + path));
        };

        WebClient webClient = WebClient.builder()
                .exchangeFunction(mockExchange)
                .build();

        return new PaymentClientV2(webClient);
    }

}
