package ru.practicum.java.internet_shop_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.dto.CartItemDto;
import ru.practicum.java.internet_shop_project.dto.CartWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class CartService {

    private static final Long SINGLETON_CARD_ID = 1L;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;

    public Mono<CartWithItemsDto> getCart() {
        return cartRepository.findById(SINGLETON_CARD_ID)
                .switchIfEmpty(cartRepository.save(new Cart()))
                .flatMap(cart -> cartItemRepository.findByCartId(cart.getId())
                        .flatMap(cartItem ->
                                productService.getProductById(cartItem.getProductId())
                                        .map(product -> new CartItemDto(
                                                cartItem.getId(),
                                                cartItem.getCartId(),
                                                cartItem.getQuantity(),
                                                product
                                        ))
                        )
                        .collectList()
                        .map(items -> new CartWithItemsDto(cart, items))
                );
    }

    public Flux<CartItem> getCartItems() {
        return cartItemRepository.findByCartId(SINGLETON_CARD_ID);
    }

    public Mono<Void> addProductToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            return Mono.error(new IllegalArgumentException("Product quantity must be greater than 0"));
        }

        return Mono.zip(
                getCart(),
                productRepository.findById(productId).switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
        ).flatMap(tuple -> {
            CartWithItemsDto cartWithItems = tuple.getT1();

            return cartItemRepository.findByProductIdAndCartId(productId, cartWithItems.getId())
                    .flatMap(cartItem -> {
                        cartItem.setQuantity(cartItem.getQuantity() + quantity);
                        return cartItemRepository.save(cartItem);
                    })
                    .switchIfEmpty(cartItemRepository.save(new CartItem(null, cartWithItems.getId(), productId, quantity)))
                    .then(updateCartTotalPrice(cartWithItems.getId()));
        });
    }

    public Mono<Void> removeProductFromCart(Long productId) {
        return cartItemRepository.removeItemFromCart(SINGLETON_CARD_ID, productId)
                .then(updateCartTotalPrice(SINGLETON_CARD_ID));
    }

    public Mono<Void> updateQuantity(Long productId, Integer quantity) {
        if (quantity < 0) {
            return Mono.error(new IllegalArgumentException("Product quantity must be greater than 0"));
        }

        return cartItemRepository.findByProductIdAndCartId(productId, SINGLETON_CARD_ID)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")))
                .flatMap(cartItem -> {
                    if (quantity == 0) {
                        return cartItemRepository.removeItemFromCart(SINGLETON_CARD_ID, productId);
                    } else {
                        cartItem.setQuantity(quantity);
                        return cartItemRepository.save(cartItem);
                    }
                }).then(updateCartTotalPrice(SINGLETON_CARD_ID));
    }

    public Mono<BigDecimal> getTotalPrice() {
        return getCartItems()
                .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                        .map(product -> product.getPrice().multiply(new BigDecimal(cartItem.getQuantity()))))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Mono<Void> updateCartTotalPrice(Long cartId) {
        return cartItemRepository.findByCartId(cartId)
                .flatMap(cartItem -> productRepository.findById(cartItem.getProductId())
                        .map(product -> product.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity())))
                )
                .reduce(BigDecimal::add)
                .defaultIfEmpty(BigDecimal.ZERO)
                .flatMap(totalPrice -> cartRepository.findById(cartId)
                        .flatMap(cart -> {
                            cart.setTotalPrice(totalPrice);
                            return cartRepository.save(cart);
                        })
                )
                .then();
    }

}
