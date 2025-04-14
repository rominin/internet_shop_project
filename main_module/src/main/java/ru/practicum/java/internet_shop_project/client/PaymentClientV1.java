package ru.practicum.java.internet_shop_project.client;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.java.paymentclient.api.DefaultApi;
import ru.practicum.java.paymentclient.model.PaymentRequest;
import ru.practicum.java.paymentclient.model.PaymentResponse;

import java.math.BigDecimal;

@Service
@Deprecated
public class PaymentClientV1 {

    private final DefaultApi paymentApi;

    public PaymentClientV1(final DefaultApi paymentApi) {
        this.paymentApi = paymentApi;
    }

    public Mono<BigDecimal> getBalance(Long userId) {
        return paymentApi.getBalance(userId)
                .map(response -> response.getAmount() != null ? response.getAmount() : BigDecimal.ZERO);
    }

    public Mono<Boolean> makePayment(Long userId, BigDecimal amount) {
        PaymentRequest request = new PaymentRequest().userId(userId).amount(amount);
        return paymentApi.makePayment(request)
                .map(response -> response.getStatus() == PaymentResponse.StatusEnum.OK);
    }

}
