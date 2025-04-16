package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.dto.CartItemDto;
import ru.practicum.java.internet_shop_project.dto.CartWithItemsDto;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CartService.class})
public class CartServiceUnitTest {

    private final Long USER_ID = 42L;
    private final Long CART_ID = 1L;

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @MockitoBean
    private ProductService productService;

    @Autowired
    private CartService cartService;

    @Test
    void testGetCart_success() {
        Cart cart = new Cart(CART_ID, USER_ID, BigDecimal.ZERO);
        CartItem cartItem = new CartItem(1L, CART_ID, 101L, 2);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(50));
        CartItemDto cartItemDto = new CartItemDto(1L, CART_ID, 2, product);
        CartWithItemsDto expectedDto = new CartWithItemsDto(cart, List.of(cartItemDto));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.just(cartItem));
        when(productService.getProductById(101L)).thenReturn(Mono.just(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.getCart(USER_ID))
                .assertNext(result -> {
                    assertEquals(expectedDto.getId(), result.getId());
                    assertEquals(expectedDto.getCartItems().size(), result.getCartItems().size());
                    assertEquals(expectedDto.getCartItems().get(0).getProduct(), result.getCartItems().get(0).getProduct());
                })
                .verifyComplete();
    }

    @Test
    void testAddProductToCart_NewProduct_ShouldBeAdded() {
        Cart cart = new Cart(CART_ID, USER_ID, BigDecimal.ZERO);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.empty());
        when(cartItemRepository.findByProductIdAndCartId(101L, CART_ID)).thenReturn(Mono.empty());
        when(productRepository.findById(101L)).thenReturn(Mono.just(product));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(new CartItem()));
        when(cartRepository.findById(CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.addProductToCart(USER_ID, 101L, 2))
                .verifyComplete();
    }

    @Test
    void testUpdateQuantity_positive_shouldSave() {
        Cart cart = new Cart(CART_ID, USER_ID, BigDecimal.ZERO);
        CartItem cartItem = new CartItem(1L, CART_ID, 101L, 1);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByProductIdAndCartId(101L, CART_ID)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.just(cartItem));
        when(productRepository.findById(101L)).thenReturn(Mono.just(product));
        when(cartRepository.findById(CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));
        when(productService.getProductById(101L)).thenReturn(Mono.just(product));

        StepVerifier.create(cartService.updateQuantity(USER_ID, 101L, 3))
                .verifyComplete();
    }

    @Test
    void testRemoveProductFromCart_success() {
        Cart cart = new Cart(CART_ID, USER_ID, BigDecimal.ZERO);

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.removeItemFromCart(CART_ID, 101L)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.empty());
        when(cartRepository.findById(CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.removeProductFromCart(USER_ID, 101L))
                .verifyComplete();
    }

    @Test
    void testUpdateQuantity_toZero_shouldRemoveItem() {
        Cart cart = new Cart(CART_ID, USER_ID, BigDecimal.ZERO);
        CartItem cartItem = new CartItem(1L, CART_ID, 101L, 5);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));

        when(cartRepository.findByUserId(USER_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByProductIdAndCartId(101L, CART_ID)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.removeItemFromCart(CART_ID, 101L)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(CART_ID)).thenReturn(Flux.empty());
        when(cartRepository.findById(CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.updateQuantity(USER_ID, 101L, 0))
                .verifyComplete();
    }

}
