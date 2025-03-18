package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.service.CartService;

import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public Mono<Rendering> getCart() {
        return cartService.getCart()
                .map(cart -> Rendering.view("cart")
                        .modelAttribute("cart", cart)
                        .build());
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

                        return cartService.addProductToCart(productId, quantity)
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

                                return cartService.removeProductFromCart(productId)
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

                        return cartService.updateQuantity(productId, quantity)
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
