package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
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

    @BeforeAll
    static void beforeAll(@Autowired CartRepository cartRepository) {
        cartRepository.save(new Cart());
    }

    @BeforeEach
    void setUp() {
        product1 = productRepository.save(new Product(null, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")));
        product2 = productRepository.save(new Product(null, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00")));
    }

    @Test
    void testFindInSingletonCartByProductId_success() {
        cart = cartRepository.findSingletonCart().orElse(new Cart());
        cart.setTotalPrice(java.math.BigDecimal.ZERO);

        CartItem cartItem = new CartItem(null, cart, product1, 2);
        cartItemRepository.save(cartItem);

        Optional<CartItem> foundItem = cartItemRepository.findInSingletonCartByProductId(product1.getId());
        assertThat(foundItem).isPresent();
        assertThat(foundItem.get().getProduct().getName()).isEqualTo("Laptop");
    }

    @Test
    void testFindInSingletonCart_success() {
        cart = cartRepository.findSingletonCart().orElse(new Cart());
        cart.setTotalPrice(java.math.BigDecimal.ZERO);

        CartItem cartItem1 = new CartItem(null, cart, product1, 2);
        CartItem cartItem2 = new CartItem(null, cart, product2, 1);
        cartItemRepository.saveAll(List.of(cartItem1, cartItem2));

        List<CartItem> cartItems = cartItemRepository.findInSingletonCart();
        assertThat(cartItems).hasSize(2);
    }

    @Test
    void testRemoveItemFromSingletonCart_success() {
        cart = cartRepository.findSingletonCart().orElse(new Cart());
        cart.setTotalPrice(java.math.BigDecimal.ZERO);

        CartItem cartItem = new CartItem(null, cart, product1, 2);
        cartItemRepository.save(cartItem);

        cartItemRepository.removeItemFromSingletonCart(product1.getId());

        List<CartItem> cartItems = cartItemRepository.findInSingletonCart();
        assertThat(cartItems).isEmpty();
    }

    @Test
    void testClearCartItemsInSingletonCart_success() {
        cart = cartRepository.findSingletonCart().orElse(new Cart());
        cart.setTotalPrice(java.math.BigDecimal.ZERO);

        CartItem cartItem1 = new CartItem(null, cart, product1, 2);
        CartItem cartItem2 = new CartItem(null, cart, product2, 1);
        cartItemRepository.saveAll(List.of(cartItem1, cartItem2));

        cartItemRepository.clearCartItemsInSingletonCart();

        List<CartItem> cartItems = cartItemRepository.findInSingletonCart();
        assertThat(cartItems).isEmpty();
    }

}
