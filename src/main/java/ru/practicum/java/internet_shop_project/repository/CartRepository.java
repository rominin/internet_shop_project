package ru.practicum.java.internet_shop_project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.entity.Cart;

@Repository
public interface CartRepository extends ReactiveCrudRepository<Cart, Long> {

    Mono<Cart> findById(Long cartId);

}
