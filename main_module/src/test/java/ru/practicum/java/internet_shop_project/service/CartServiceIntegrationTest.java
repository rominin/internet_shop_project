package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.config.EmbeddedRedisConfiguration;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(EmbeddedRedisConfiguration.class)
public class CartServiceIntegrationTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = productRepository.save(new Product(null, "Brand New Device"+UUID.randomUUID(), "imageUrl", "Some Brand New Device", new BigDecimal("1500.00"))).block();
        product2 = productRepository.save(new Product(null, "Smartphone"+UUID.randomUUID(), "imageUrl", "Some Smartphone", new BigDecimal("800.00"))).block();
        cartItemRepository.deleteAll().block();
    }

    @Test
    void testGetCart_success() {
        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertThat(cart).isNotNull();
                })
                .verifyComplete();
    }

    @Test
    void testAddProductToCart_success() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), 2))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertThat(cart.getCartItems()).hasSize(1);
                    assertThat(cart.getCartItems().getFirst().getProduct().getName()).contains("Brand New Device");
                    assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(2);

                    BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(2));
                    assertThat(cart.getTotalPrice()).isEqualByComparingTo(expectedTotal);
                })
                .verifyComplete();
    }

    @Test
    void testUpdateQuantity_success() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), 2))
                .verifyComplete();

        StepVerifier.create(cartService.updateQuantity(product1.getId(), 3))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertThat(cart.getCartItems().getFirst().getQuantity()).isEqualTo(3);
                    BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(3));
                    assertThat(cart.getTotalPrice()).isEqualByComparingTo(expectedTotal);
                })
                .verifyComplete();
    }

    @Test
    void testUpdateQuantityToZero_success() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), 2))
                .verifyComplete();

        StepVerifier.create(cartService.updateQuantity(product1.getId(), 0))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertThat(cart.getCartItems()).isEmpty();
                    assertThat(cart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    @Test
    void testRemoveProductFromCart_success() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), 2))
                .verifyComplete();

        StepVerifier.create(cartService.addProductToCart(product2.getId(), 1))
                .verifyComplete();

        StepVerifier.create(cartService.removeProductFromCart(product1.getId()))
                .verifyComplete();

        StepVerifier.create(cartService.getCart())
                .assertNext(cart -> {
                    assertThat(cart.getCartItems()).hasSize(1);
                    assertThat(cart.getCartItems().getFirst().getProduct().getName()).contains("Smartphone");

                    BigDecimal expectedTotal = product2.getPrice();
                    assertThat(cart.getTotalPrice()).isEqualByComparingTo(expectedTotal);
                })
                .verifyComplete();
    }

    @Test
    void testAddProductToCart_failure() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), -1))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Product quantity must be greater than 0"))
                .verify();
    }

    @Test
    void testUpdateQuantity_failure() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), 1))
                .verifyComplete();

        StepVerifier.create(cartService.updateQuantity(product1.getId(), -2))
                .expectErrorMatches(throwable -> throwable instanceof IllegalArgumentException &&
                        throwable.getMessage().equals("Product quantity must be greater than 0"))
                .verify();
    }

    @Test
    void testGetTotalPrice_success() {
        StepVerifier.create(cartService.addProductToCart(product1.getId(), 2))
                .verifyComplete();

        StepVerifier.create(cartService.addProductToCart(product2.getId(), 1))
                .verifyComplete();

        StepVerifier.create(cartService.getTotalPrice())
                .assertNext(totalPrice -> {
                    BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(2))
                            .add(product2.getPrice().multiply(BigDecimal.ONE));

                    assertThat(totalPrice).isEqualByComparingTo(expectedTotal);
                })
                .verifyComplete();
    }

}
