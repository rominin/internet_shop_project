package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.client.PaymentClientV2;
import ru.practicum.java.internet_shop_project.dto.CartWithItemsDto;
import ru.practicum.java.internet_shop_project.service.CartService;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    private final PaymentClientV2 paymentClient;

    @GetMapping
    public Mono<Rendering> getCart(@RequestParam(name = "errorMessage", required = false) String errorMessage) {
        return cartService.getCart(1L)  // TODO real Id
                .zipWith(paymentClient.getBalance(1L)  // TODO real Id
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
                });
    }

    @PostMapping("/add")
    public Mono<Rendering> addToCart(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    Part productIdPart = multipartData.getFirst("productId");
                    Part quantityPart = multipartData.getFirst("quantity");

                    return Mono.zip(
                            extractValue(productIdPart),
                            extractValue(quantityPart),
                            Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Referer"))
                    ).flatMap(tuple -> {
                        Long productId = Long.parseLong(tuple.getT1());
                        Integer quantity = Integer.parseInt(tuple.getT2());
                        String referer = tuple.getT3();

                        return cartService.addProductToCart(1L, productId, quantity)// TODO real Id
                                .then(Mono.just(Rendering.view(referer != null ? "redirect:" + referer : "redirect:/cart").build()));
                    });
                });
    }

    @PostMapping("/remove")
    public Mono<Rendering> removeFromCart(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    Part productIdPart = multipartData.getFirst("productId");

                    return extractValue(productIdPart)
                            .flatMap(productIdStr -> {
                                Long productId = Long.parseLong(productIdStr);
                                String referer = exchange.getRequest().getHeaders().getFirst("Referer");

                                return cartService.removeProductFromCart(1L, productId)// TODO real Id
                                        .then(Mono.just(Rendering.view(referer != null ? "redirect:" + referer : "redirect:/cart").build()));
                            });
                });
    }

    @PostMapping("/update")
    public Mono<Rendering> updateCartItem(ServerWebExchange exchange) {
        return exchange.getMultipartData()
                .flatMap(multipartData -> {
                    Part productIdPart = multipartData.getFirst("productId");
                    Part quantityPart = multipartData.getFirst("quantity");

                    return Mono.zip(
                            extractValue(productIdPart),
                            extractValue(quantityPart),
                            Mono.justOrEmpty(exchange.getRequest().getHeaders().getFirst("Referer"))
                    ).flatMap(tuple -> {
                        Long productId = Long.parseLong(tuple.getT1());
                        Integer quantity = Integer.parseInt(tuple.getT2());
                        String referer = tuple.getT3();

                        return cartService.updateQuantity(1L, productId, quantity)// TODO real Id
                                .then(Mono.just(Rendering.view(referer != null ? "redirect:" + referer : "redirect:/cart").build()));
                    });
                });
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
