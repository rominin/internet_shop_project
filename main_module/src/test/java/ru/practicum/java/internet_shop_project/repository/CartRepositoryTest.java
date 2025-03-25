package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.entity.Cart;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class CartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    private Cart cart;

    @BeforeAll
    static void beforeAll(@Autowired CartRepository cartRepository) {
        cartRepository.save(new Cart());
    }

    @BeforeEach
    void setUp() {
        cart = new Cart();
        cartRepository.save(cart)
                .as(StepVerifier::create)
                .assertNext(savedCart -> {
                    assertThat(savedCart.getId()).isNotNull();
                    cart.setId(savedCart.getId());
                })
                .verifyComplete();
    }

    @Test
    void testFindById_success() {
        StepVerifier.create(cartRepository.findById(cart.getId()))
                .assertNext(foundCart -> {
                    assertThat(foundCart).isNotNull();
                    assertThat(foundCart.getId()).isEqualTo(cart.getId());
                    assertThat(foundCart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
                })
                .verifyComplete();
    }

    @Test
    void testFindById_notFound() {
        StepVerifier.create(cartRepository.findById(999L))
                .expectSubscription()
                .expectNextCount(0)
                .verifyComplete();
    }

}
