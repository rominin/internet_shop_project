package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Cart;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Transactional
@ActiveProfiles("test")
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
        cart = cartRepository.findSingletonCart().orElse(new Cart());
        cart.setTotalPrice(java.math.BigDecimal.ZERO);
        cart = cartRepository.save(cart);
    }

    @Test
    void testSaveCart_success() {
        Optional<Cart> savedCart = cartRepository.findById(cart.getId());
        assertThat(savedCart).isPresent();
        assertThat(savedCart.get().getId()).isEqualTo(cart.getId());
    }

    @Test
    void testFindSingletonCart_success() {
        Optional<Cart> singletonCart = cartRepository.findSingletonCart();

        assertThat(singletonCart).isPresent();
        assertThat(singletonCart.get().getId()).isEqualTo(cart.getId());
    }

    @Test
    void testFindSingletonCart_shouldReturnEmptyIfNoCartExists() {
        cartRepository.deleteAll();

        Optional<Cart> singletonCart = cartRepository.findSingletonCart();
        assertThat(singletonCart).isEmpty();
    }

    @Test
    void testDeleteCart_success() {
        cartRepository.delete(cart);

        Optional<Cart> deletedCart = cartRepository.findById(cart.getId());
        assertThat(deletedCart).isEmpty();
    }

}
