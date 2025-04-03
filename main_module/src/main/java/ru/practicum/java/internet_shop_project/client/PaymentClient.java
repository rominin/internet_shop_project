package ru.practicum.java.internet_shop_project.client;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.java.paymentclient.api.DefaultApi;
import ru.practicum.java.paymentclient.model.PaymentRequest;
import ru.practicum.java.paymentclient.model.PaymentResponse;

import java.math.BigDecimal;

@Service
public class PaymentClient {

    private final DefaultApi paymentApi;

    public PaymentClient(final DefaultApi paymentApi) {
        this.paymentApi = paymentApi;
    }

    public Mono<BigDecimal> getBalance() {
        return paymentApi.getBalance()
                .map(response -> response.getAmount() != null ? response.getAmount() : BigDecimal.ZERO);
    }

    public Mono<Boolean> makePayment(BigDecimal amount) {
        PaymentRequest request = new PaymentRequest().amount(amount);
        return paymentApi.makePayment(request)
                .map(response -> response.getStatus() == PaymentResponse.StatusEnum.OK);
    }

}
