package ru.practicum.java.payment_service.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.java.payment_service.api.DefaultApi;
import ru.practicum.java.payment_service.model.BalanceResponse;
import ru.practicum.java.payment_service.model.PaymentRequest;
import ru.practicum.java.payment_service.model.PaymentResponse;
import ru.practicum.java.payment_service.service.PaymentService;

@RestController
public class PaymentController implements DefaultApi {

    private final PaymentService paymentService;

    public PaymentController(final PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(ServerWebExchange exchange) {
        return paymentService.getBalance(1L)// TODO real Id
                .map(balance -> ResponseEntity.ok(new BalanceResponse().amount(balance)));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> makePayment(Mono<PaymentRequest> paymentRequest, ServerWebExchange exchange) {
        return paymentRequest
                .flatMap(request -> paymentService.pay(1L, request.getAmount())// TODO real Id
                        .map(success -> {
                            PaymentResponse paymentResponse = new PaymentResponse()
                                    .status(success ? PaymentResponse.StatusEnum.OK : PaymentResponse.StatusEnum.FAILED);
                            return ResponseEntity.ok(paymentResponse);
                        })
                        .onErrorResume(exception -> {
                            PaymentResponse paymentResponse = new PaymentResponse()
                                    .status(PaymentResponse.StatusEnum.FAILED);
                            return Mono.just(ResponseEntity.badRequest().body(paymentResponse));
                        })
                );
    }
}
