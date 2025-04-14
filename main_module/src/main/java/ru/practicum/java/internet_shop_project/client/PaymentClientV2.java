package ru.practicum.java.internet_shop_project.client;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.practicum.java.paymentclient.model.BalanceResponse;
import ru.practicum.java.paymentclient.model.PaymentRequest;
import ru.practicum.java.paymentclient.model.PaymentResponse;

import java.math.BigDecimal;

@Service
public class PaymentClientV2 {

    private final WebClient paymentWebClient;

    public PaymentClientV2(WebClient paymentWebClient) {
        this.paymentWebClient = paymentWebClient;
    }

    public Mono<BigDecimal> getBalance(Long userId) {
        return paymentWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/balance")
                        .queryParam("userId", userId)
                        .build())
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(res -> res.getAmount() != null ? res.getAmount() : BigDecimal.ZERO);
    }

    public Mono<Boolean> makePayment(Long userId, BigDecimal amount) {
        PaymentRequest request = new PaymentRequest()
                .userId(userId)
                .amount(amount);

        return paymentWebClient.post()
                .uri("/pay")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .map(res -> res.getStatus() == PaymentResponse.StatusEnum.OK);
    }

}
