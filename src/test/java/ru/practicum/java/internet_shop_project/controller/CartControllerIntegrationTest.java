package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class CartControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ProductRepository productRepository;

    private Product product;
    private Cart cart;

    @BeforeAll
    static void beforeAll(@Autowired CartRepository cartRepository) {
        cartRepository.save(new Cart());
    }

    @BeforeEach
    void setUp() {
//        cartItemRepository.deleteAll();
//        cartRepository.deleteAll();
//        productRepository.deleteAll();

        cart = cartRepository.findSingletonCart().orElse(new Cart());
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        product = productRepository.save(new Product(null, "Laptop", "testUrl", "Some Laptop", new BigDecimal("1500.00")));
    }

    @Test
    void testGetCart_success() throws Exception {
        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cart"));
    }

    @Test
    void testAddToCart_success() throws Exception {
        mockMvc.perform(post("/cart/add")
                        .param("productId", product.getId().toString())
                        .param("quantity", "1"))
                .andExpect(status().is3xxRedirection());

        List<CartItem> items = cartItemRepository.findInSingletonCart();
        assertThat(items).isNotEmpty();
        assertThat(items.getFirst().getProduct().getName()).isEqualTo("Laptop");
    }

    @Test
    void testRemoveFromCart_success() throws Exception {
        CartItem cartItem = cartItemRepository.save(new CartItem(null, cart, product, 1));

        mockMvc.perform(post("/cart/remove")
                        .param("productId", product.getId().toString()))
                .andExpect(status().is3xxRedirection());

        assertThat(cartItemRepository.findInSingletonCart()).isEmpty();
    }

    @Test
    void testUpdateCartItem_success() throws Exception {
        CartItem cartItem = cartItemRepository.save(new CartItem(null, cart, product, 1));

        mockMvc.perform(post("/cart/update")
                        .param("productId", product.getId().toString())
                        .param("quantity", "3"))
                .andExpect(status().is3xxRedirection());

        CartItem updatedItem = cartItemRepository.findInSingletonCart().getFirst();
        assertThat(updatedItem.getQuantity()).isEqualTo(3);
    }

}

