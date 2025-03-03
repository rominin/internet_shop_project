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
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class OrderServiceIntegrationTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderService orderService;

    @Autowired
    private CartService cartService;

    private Product product1;
    private Product product2;
    private Cart cart;

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
    void testCreateOrderFromCart_success() {
        cart = cartService.getCart();

        CartItem cartItem1 = new CartItem(null, cart, product1, 2);
        CartItem cartItem2= new  CartItem(null, cart, product2, 1);
        cartItemRepository.save(cartItem1);
        cartItemRepository.save(cartItem2);

        cart.setTotalPrice(new BigDecimal("3800.00"));
        setCartItemsProperly(List.of(cartItem1, cartItem2));
        cartRepository.save(cart);

        Order order = orderService.createOrderFromCart();

        assertNotNull(order);
        assertThat(order.getId()).isNotNull();
        assertThat(order.getTotalPrice()).isEqualByComparingTo(new BigDecimal("3800.00"));

        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    @Test
    @Transactional
    void testGetOrderById_success() {
        cart = cartService.getCart();

        CartItem cartItem1 = new CartItem(null, cart, product1, 1);
        cartItemRepository.save(cartItem1);

        cart.setTotalPrice(new BigDecimal("1500.00"));
        setCartItemsProperly(List.of(cartItem1));
        cartRepository.save(cart);

        Order createdOrder = orderService.createOrderFromCart();

        Order retrievedOrder = orderService.getOrderById(createdOrder.getId());

        assertNotNull(retrievedOrder);
        assertThat(retrievedOrder.getId()).isEqualTo(createdOrder.getId());
        assertThat(retrievedOrder.getTotalPrice()).isEqualByComparingTo(createdOrder.getTotalPrice());
    }

    @Test
    @Transactional
    void testGetAllOrders_success() {
        cart = cartService.getCart();

        CartItem cartItem1 = new CartItem(null, cart, product1, 1);
        cartItemRepository.save(cartItem1);

        cart.setTotalPrice(new BigDecimal("1500.00"));
        setCartItemsProperly(List.of(cartItem1));
        cartRepository.save(cart);

        orderService.createOrderFromCart();

        CartItem cartItem2 = new CartItem(null, cart, product2, 1);
        cartItemRepository.save(cartItem2);

        cart.setTotalPrice(new BigDecimal("800.00"));
        setCartItemsProperly(List.of(cartItem2));
        cartRepository.save(cart);

        orderService.createOrderFromCart();

        List<Order> orders = orderService.getAllOrders();

        assertThat(orders).hasSize(2);
    }

    @Test
    @Transactional
    void testGetTotalOrdersPrice_success() {
        cart = cartService.getCart();

        CartItem cartItem1 = new CartItem(null, cart, product1, 2);
        cartItemRepository.save(cartItem1);

        cart.setTotalPrice(new BigDecimal("3000.00"));
        setCartItemsProperly(List.of(cartItem1));
        cartRepository.save(cart);

        orderService.createOrderFromCart();

        CartItem cartItem2 = new CartItem(null, cart, product2, 1);
        cartItemRepository.save(cartItem2);

        cart.setTotalPrice(new BigDecimal("800.00"));
        setCartItemsProperly(List.of(cartItem2));
        cartRepository.save(cart);

        orderService.createOrderFromCart();

        BigDecimal totalOrdersPrice = orderService.getTotalOrdersPrice();
        assertThat(totalOrdersPrice).isEqualByComparingTo(new BigDecimal("3800.00"));
    }

    @Test
    @Transactional
    void testCreateOrderFromEmptyCart_shouldThrowException() {
        Exception exception = assertThrows(RuntimeException.class, orderService::createOrderFromCart);
        assertThat(exception.getMessage()).isEqualTo("Cart is empty");
    }

    private void setCartItemsProperly(List<CartItem> cartItems) {
        if (cart.getCartItems() != null) {
            cartItems.forEach(cartItem -> {cart.getCartItems().add(cartItem);});
        } else {
            cart.setCartItems(new ArrayList<>(cartItems));
        }
    }
}