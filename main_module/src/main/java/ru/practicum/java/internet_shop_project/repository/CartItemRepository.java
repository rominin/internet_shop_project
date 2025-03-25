package ru.practicum.java.internet_shop_project.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.entity.CartItem;

@Repository
public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Mono<CartItem> findByProductIdAndCartId(Long productId, Long cartId);

    Flux<CartItem> findByCartId(Long cartId);

    @Query("DELETE FROM cart_items WHERE cart_id = :cartId AND product_id = :productId")
    Mono<Void> removeItemFromCart(Long cartId, Long productId);

    @Query("DELETE FROM cart_items WHERE cart_id = :cartId")
    Mono<Void> clearCartItems(Long cartId);

}
