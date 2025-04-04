package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private ProductRepository productRepository;

    private Cart cart;
    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        cart = new Cart();
        product1 = new Product(null, "Laptop" + UUID.randomUUID(), "someUrl", "Some Laptop", new BigDecimal("1500.00"));
        product2 = new Product(null, "Phone" + UUID.randomUUID(), "someUrl", "Some Smartphone", new BigDecimal("800.00"));

        StepVerifier.create(
                cartRepository.save(cart)
                        .flatMap(savedCart -> {
                            cart = savedCart;
                            return productRepository.save(product1);
                        })
                        .flatMap(savedProduct1 -> {
                            product1 = savedProduct1;
                            return productRepository.save(product2);
                        })
                        .flatMap(savedProduct2 -> {
                            product2 = savedProduct2;
                            return Mono.empty();
                        })
        ).verifyComplete();
    }

    @Test
    void testFindByProductIdAndCartId_success() {
        CartItem cartItem = new CartItem(null, cart.getId(), product1.getId(), 2);

        StepVerifier.create(cartItemRepository.save(cartItem))
                .expectNextMatches(savedItem -> savedItem.getProductId().equals(product1.getId()))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByProductIdAndCartId(product1.getId(), cart.getId()))
                .expectNextMatches(foundItem ->
                        foundItem.getProductId().equals(product1.getId()) && foundItem.getQuantity() == 2)
                .verifyComplete();
    }

    @Test
    void testFindByCartId_success() {
        CartItem cartItem1 = new CartItem(null, cart.getId(), product1.getId(), 2);
        CartItem cartItem2 = new CartItem(null, cart.getId(), product2.getId(), 1);

        StepVerifier.create(cartItemRepository.saveAll(Flux.just(cartItem1, cartItem2)).then())
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartId(cart.getId()))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void testRemoveItemFromCart_success() {
        CartItem cartItem = new CartItem(null, cart.getId(), product1.getId(), 2);

        StepVerifier.create(cartItemRepository.save(cartItem))
                .expectNextCount(1)
                .verifyComplete();

        StepVerifier.create(cartItemRepository.removeItemFromCart(cart.getId(), product1.getId()))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartId(cart.getId()))
                .verifyComplete();
    }

    @Test
    void testClearCartItems_success() {
        CartItem cartItem1 = new CartItem(null, cart.getId(), product1.getId(), 2);
        CartItem cartItem2 = new CartItem(null, cart.getId(), product2.getId(), 1);

        StepVerifier.create(cartItemRepository.saveAll(Flux.just(cartItem1, cartItem2)).then())
                .verifyComplete();

        StepVerifier.create(cartItemRepository.clearCartItems(cart.getId()))
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartId(cart.getId()))
                .verifyComplete();
    }

}
