package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.Part;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClientV2;
import ru.practicum.java.internet_shop_project.dto.CartWithItemsDto;
import ru.practicum.java.internet_shop_project.service.CartService;
import ru.practicum.java.internet_shop_project.service.UserContextService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private final PaymentClientV2 paymentClient;

    private final UserContextService userContextService;

    @GetMapping
    public Mono<Rendering> getCart(@RequestParam(name = "errorMessage", required = false) String errorMessage, Authentication authentication) {
        return userContextService.getCurrentUserId(authentication)
                .flatMap(userId -> cartService.getCart(userId)
                        .zipWith(paymentClient.getBalance(userId)
                                .onErrorResume(ex -> Mono.just(BigDecimal.valueOf(-1)))
                        )
                        .map(tuple -> {
                            CartWithItemsDto cart = tuple.getT1();
                            BigDecimal balance = tuple.getT2();

                            boolean canCheckout = false;
                            String checkoutMessage = errorMessage;

                            if (checkoutMessage == null) {
                                if (balance.compareTo(BigDecimal.ZERO) < 0) {
                                    checkoutMessage = "Сервис платежей недоступен";
                                } else if (balance.compareTo(cart.getTotalPrice()) < 0) {
                                    checkoutMessage = "Недостаточно средств для оформления заказа";
                                } else {
                                    canCheckout = true;
                                }
                            }

                            return Rendering.view("cart")
                                    .modelAttribute("cart", cart)
                                    .modelAttribute("canCheckout", canCheckout)
                                    .modelAttribute("checkoutMessage", checkoutMessage)
                                    .build();
                        })
                );
    }

    @PostMapping("/add")
    public Mono<Void> addToCart(ServerWebExchange exchange, Authentication authentication) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    Part productIdPart = multipartData.getFirst("productId");
                    Part quantityPart = multipartData.getFirst("quantity");

                    return Mono.zip(
                            extractValue(productIdPart),
                            extractValue(quantityPart)
                    ).flatMap(tuple -> {
                        Long productId = Long.parseLong(tuple.getT1());
                        Integer quantity = Integer.parseInt(tuple.getT2());

                        return userContextService.getCurrentUserId(authentication)
                                .flatMap(userId -> cartService.addProductToCart(userId, productId, quantity));
                    });
                })
                .then();
    }

    @PostMapping("/remove")
    public Mono<Void> removeFromCart(ServerWebExchange exchange, Authentication authentication) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    Part productIdPart = multipartData.getFirst("productId");

                    return extractValue(productIdPart)
                            .flatMap(productIdStr -> {
                                Long productId = Long.parseLong(productIdStr);

                                return userContextService.getCurrentUserId(authentication)
                                        .flatMap(userId -> cartService.removeProductFromCart(userId, productId));
                            });
                });
    }

    @PostMapping("/update")
    public Mono<Void> updateCartItem(ServerWebExchange exchange, Authentication authentication) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    Part productIdPart = multipartData.getFirst("productId");
                    Part quantityPart = multipartData.getFirst("quantity");

                    return Mono.zip(
                            extractValue(productIdPart),
                            extractValue(quantityPart)
                    ).flatMap(tuple -> {
                        Long productId = Long.parseLong(tuple.getT1());
                        Integer quantity = Integer.parseInt(tuple.getT2());

                        return userContextService.getCurrentUserId(authentication)
                                .flatMap(userId -> cartService.updateQuantity(userId, productId, quantity));
                    });
                })
                .then();
    }

    private Mono<String> extractValue(Part part) {
        return DataBufferUtils.join(part.content())
                .map(buffer -> {
                    byte[] bytes = new byte[buffer.readableByteCount()];
                    buffer.read(bytes);
                    DataBufferUtils.release(buffer);
                    return new String(bytes, StandardCharsets.UTF_8);
                });
    }

}
