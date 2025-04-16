package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.config.MockedPaymentClientConfiguration;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.entity.User;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;
import ru.practicum.java.internet_shop_project.repository.UserRepository;

import java.math.BigDecimal;
import java.util.UUID;


@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(MockedPaymentClientConfiguration.class)
class OrderServiceIntegrationTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderService orderService;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = productRepository.save(new Product(null, "Laptop" + UUID.randomUUID(), "imageUrl", "Some Laptop", new BigDecimal("1500.00"))).block();
        product2 = productRepository.save(new Product(null, "Phone" + UUID.randomUUID(), "imageUrl", "Some Smartphone", new BigDecimal("800.00"))).block();
    }

    @Test
    void testCreateOrderFromEmptyCart_shouldThrowException() {
        User user = new User();
        user.setUsername("testuser");
        user.setRole("CUSTOMER");

        Long userId = userRepository.save(user)
                .map(User::getId)
                .block();

        Cart emptyCart = new Cart(null, userId, BigDecimal.ZERO);
        cartRepository.save(emptyCart).block();

        StepVerifier.create(orderService.createOrderFromCart(userId))
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Cart is empty"))
                .verify();
    }

}