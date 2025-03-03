package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class CartServiceIntegrationTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    private Product product1;
    private Product product2;

    @BeforeAll
    static void beforeAll(@Autowired CartService cartService) {
        cartService.getCart();
    }

    @BeforeEach
    void setUp() {
        product1 = productRepository.save(new Product(null, "Laptop", "imageUrl", "Some Laptop", new BigDecimal("1500.00")));
        product2 = productRepository.save(new Product(null, "Phone", "imageUrl", "Some Smartphone", new BigDecimal("800.00")));
    }

    @Test
    @Transactional
    void testGetCart_success() {
        Cart foundCart = cartService.getCart();
        assertThat(foundCart).isNotNull();
        assertThat(foundCart.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    void testAddProductToCart_success() {
        cartService.addProductToCart(product1.getId(), 2);

        List<CartItem> cartItems = cartService.getCartItems();
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().getProduct().getName()).isEqualTo("Laptop");
        assertThat(cartItems.getFirst().getQuantity()).isEqualTo(2);

        BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(2));
        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @Transactional
    void testUpdateQuantity_success() {
        cartService.addProductToCart(product1.getId(), 2);
        cartService.updateQuantity(product1.getId(), 3);

        CartItem updatedItem = cartItemRepository.findInSingletonCartByProductId(product1.getId()).orElseThrow();
        assertThat(updatedItem.getQuantity()).isEqualTo(3);

        BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(3));
        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @Transactional
    void testUpdateQuantityToZero_success() {
        cartService.addProductToCart(product1.getId(), 2);
        cartService.updateQuantity(product1.getId(), 0);

        List<CartItem> cartItems = cartService.getCartItems();
        assertThat(cartItems).isEmpty();
        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @Transactional
    void testRemoveProductFromCart_success() {
        cartService.addProductToCart(product1.getId(), 2);
        cartService.addProductToCart(product2.getId(), 1);

        cartService.removeProductFromCart(product1.getId());

        List<CartItem> cartItems = cartService.getCartItems();
        assertThat(cartItems).hasSize(1);
        assertThat(cartItems.getFirst().getProduct().getName()).isEqualTo("Phone");

        BigDecimal expectedTotal = product2.getPrice().multiply(BigDecimal.valueOf(1));
        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }

    @Test
    @Transactional
    void testAddProductToCart_failure() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> cartService.addProductToCart(product1.getId(), -1));
        assertThat(exception.getMessage()).isEqualTo("Product quantity must be greater than 0");
    }

    @Test
    @Transactional
    void testUpdateQuantity_failure() {
        cartService.addProductToCart(product1.getId(), 1);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> cartService.updateQuantity(product1.getId(), -2));
        assertThat(exception.getMessage()).isEqualTo("Product quantity must be greater than 0");
    }

    @Test
    @Transactional
    void testGetTotalPrice_success() {
        cartService.addProductToCart(product1.getId(), 2);
        cartService.addProductToCart(product2.getId(), 1);

        BigDecimal expectedTotal = product1.getPrice().multiply(BigDecimal.valueOf(2))
                .add(product2.getPrice().multiply(BigDecimal.ONE));

        assertThat(cartService.getTotalPrice()).isEqualByComparingTo(expectedTotal);
    }

}
