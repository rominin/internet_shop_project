package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class OrderServiceIntegrationTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    private Product product1;
    private Product product2;
    private final Long SINGLETON_CART_ID = 1L;

    @BeforeEach
    void setUp() {
        product1 = productRepository.save(new Product(null, "Laptop", "imageUrl", "Some Laptop", new BigDecimal("1500.00"))).block();
        product2 = productRepository.save(new Product(null, "Phone", "imageUrl", "Some Smartphone", new BigDecimal("800.00"))).block();
    }

    @Test
    void testCreateOrderFromCart_success() {
        Cart cart = new Cart();
        cartRepository.save(cart).block();

        CartItem cartItem1 = new CartItem(null, SINGLETON_CART_ID, product1.getId(), 2);
        CartItem cartItem2 = new CartItem(null, SINGLETON_CART_ID, product2.getId(), 1);

        cartItemRepository.saveAll(Flux.just(cartItem1, cartItem2)).blockLast();
        cart.setTotalPrice(new BigDecimal("3800.00"));
        cartRepository.save(cart).block();

        StepVerifier.create(orderService.createOrderFromCart())
                .assertNext(order -> {
                    assertThat(order.getId()).isNotNull();
                    assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("3800.00"));
                })
                .verifyComplete();

        StepVerifier.create(cartItemRepository.findByCartId(SINGLETON_CART_ID))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testGetOrderById_success() {
        Cart cart = new Cart();
        cart.setTotalPrice(new BigDecimal("1500.00"));
        Cart savedCart = cartRepository.save(cart).block();
        Long cartId = savedCart.getId();

        CartItem cartItem = new CartItem(null, cartId, product1.getId(), 1);
        cartItemRepository.save(cartItem).block();

        Order createdOrder = orderService.createOrderFromCart().block();

        assertThat(createdOrder).isNotNull();
        assertThat(createdOrder.getId()).isNotNull();
        assertThat(createdOrder.getTotalPrice()).isEqualByComparingTo(new BigDecimal("1500.00"));

        StepVerifier.create(orderService.getOrderById(createdOrder.getId()))
                .assertNext(orderDto -> {
                    assertThat(orderDto.getId()).isEqualTo(createdOrder.getId());
                    assertThat(orderDto.getTotalPrice()).isEqualByComparingTo(new BigDecimal("1500.00"));
                    assertThat(orderDto.getOrderItems()).hasSize(1);
                    assertThat(orderDto.getOrderItems().get(0).getProduct().getId()).isEqualTo(product1.getId());
                })
                .verifyComplete();
    }

    @Test
    void testGetAllOrders_success() {
        Cart cart = new Cart();
        cart.setTotalPrice(new BigDecimal("2300.00"));
        cartRepository.save(cart).block();

        CartItem cartItem1 = new CartItem(null, cart.getId(), product1.getId(), 1);
        CartItem cartItem2 = new CartItem(null, cart.getId(), product2.getId(), 1);
        cartItemRepository.saveAll(Flux.just(cartItem1, cartItem2)).blockLast();

        orderService.createOrderFromCart().block();

        cart.setTotalPrice(new BigDecimal("800.00"));
        cartRepository.save(cart).block();

        CartItem cartItem3 = new CartItem(null, cart.getId(), product2.getId(), 1);
        cartItemRepository.save(cartItem3).block();

        orderService.createOrderFromCart().block();

        StepVerifier.create(orderService.getAllOrders())
                .recordWith(ArrayList::new)
                .thenConsumeWhile(Objects::nonNull)
                .consumeRecordedWith(orders -> {
                    assertThat(orders).hasSize(2);
                })
                .verifyComplete();
    }

    @Test
    void testGetTotalOrdersPrice_success() {
        Cart cart = new Cart();
        cart.setTotalPrice(new BigDecimal("3800.00"));
        cartRepository.save(cart).block();

        CartItem cartItem1 = new CartItem(null, SINGLETON_CART_ID, product1.getId(), 2);
        CartItem cartItem2 = new CartItem(null, SINGLETON_CART_ID, product2.getId(), 1);
        cartItemRepository.saveAll(Flux.just(cartItem1, cartItem2)).blockLast();

        orderService.createOrderFromCart().block();

        StepVerifier.create(orderService.getTotalOrdersPrice())
                .assertNext(totalPrice -> assertThat(totalPrice).isEqualByComparingTo(new BigDecimal("3800.00")))
                .verifyComplete();
    }

    @Test
    void testCreateOrderFromEmptyCart_shouldThrowException() {
        Cart cart = new Cart();
        cartRepository.save(cart).block();

        StepVerifier.create(orderService.createOrderFromCart())
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Cart is empty"))
                .verify();
    }

}