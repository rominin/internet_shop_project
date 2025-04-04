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

    private final Long SINGLETON_CART_ID = 1L;

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
        Cart cart = new Cart(SINGLETON_CART_ID, BigDecimal.ZERO);
        CartItem cartItem = new CartItem(1L, SINGLETON_CART_ID, 101L, 2);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(50));
        CartItemDto cartItemDto = new CartItemDto(1L, SINGLETON_CART_ID, 2, product);
        CartWithItemsDto expectedDto = new CartWithItemsDto(cart, List.of(cartItemDto));

        when(cartRepository.findById(SINGLETON_CART_ID)).thenReturn(Mono.just(cart));
        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.just(cartItem));
        when(productService.getProductById(101L)).thenReturn(Mono.just(product));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.getCart())
                .assertNext(result -> {
                    assert result.getId().equals(expectedDto.getId());
                    assert result.getTotalPrice().equals(expectedDto.getTotalPrice());
                    assert result.getCartItems().size() == 1;
                    assert result.getCartItems().get(0).getProduct().equals(product);
                })
                .verifyComplete();
    }

    @Test
    void testGetCartItems_success() {
        CartItem cartItem1 = new CartItem(1L, SINGLETON_CART_ID, 101L, 2);
        CartItem cartItem2 = new CartItem(2L, SINGLETON_CART_ID, 102L, 3);

        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.just(cartItem1, cartItem2));

        StepVerifier.create(cartService.getCartItems())
                .expectNext(cartItem1, cartItem2)
                .verifyComplete();

        verify(cartItemRepository, times(1)).findByCartId(SINGLETON_CART_ID);
    }

    @Test
    void testAddProductToCart_NewProduct_ShouldBeAdded() {
        Cart cart = new Cart(SINGLETON_CART_ID, BigDecimal.ZERO);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));
        CartItem newCartItem = new CartItem(null, SINGLETON_CART_ID, 101L, 2);

        when(cartRepository.findById(SINGLETON_CART_ID)).thenReturn(Mono.just(cart));
        when(productRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(cartItemRepository.findByProductIdAndCartId(product.getId(), SINGLETON_CART_ID)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.empty());
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(newCartItem));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.addProductToCart(product.getId(), 2))
                .verifyComplete();

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testAddProductToCart_ExistingProduct_ShouldIncreaseQuantity() {
        Cart cart = new Cart(SINGLETON_CART_ID, BigDecimal.ZERO);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));
        CartItem cartItem = new CartItem(1L, SINGLETON_CART_ID, 101L, 1);

        when(cartRepository.findById(SINGLETON_CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.just(cart));

        when(productService.getProductById(product.getId())).thenReturn(Mono.just(product));
        when(productRepository.findById(product.getId())).thenReturn(Mono.just(product));
        when(cartItemRepository.findByProductIdAndCartId(product.getId(), SINGLETON_CART_ID)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(any(CartItem.class))).thenReturn(Mono.just(cartItem));

        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.just(cartItem));

        StepVerifier.create(cartService.addProductToCart(product.getId(), 2))
                .verifyComplete();

        assertEquals(3, cartItem.getQuantity());
        verify(cartItemRepository, times(2)).save(any(CartItem.class));
    }

    @Test
    void testRemoveProductFromCart_success() {
        when(cartItemRepository.removeItemFromCart(SINGLETON_CART_ID, 101L)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.empty());
        when(cartRepository.findById(SINGLETON_CART_ID)).thenReturn(Mono.just(new Cart(SINGLETON_CART_ID, BigDecimal.ZERO)));
        when(cartRepository.save(any(Cart.class))).thenReturn(Mono.empty());

        StepVerifier.create(cartService.removeProductFromCart(101L))
                .verifyComplete();

        verify(cartItemRepository, times(1)).removeItemFromCart(SINGLETON_CART_ID, 101L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testUpdateQuantity_ShouldUpdateQuantity() {
        Cart cart = new Cart(SINGLETON_CART_ID, BigDecimal.ZERO);
        CartItem cartItem = new CartItem(1L, SINGLETON_CART_ID, 101L, 1);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));

        when(cartItemRepository.findByProductIdAndCartId(101L, SINGLETON_CART_ID)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.save(cartItem)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.just(cartItem));
        when(productRepository.findById(cartItem.getProductId())).thenReturn(Mono.just(product));
        when(cartRepository.findById(SINGLETON_CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(cart)).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.updateQuantity(101L, 5))
                .verifyComplete();

        assertEquals(5, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testUpdateQuantity_ToZero_ShouldRemoveProduct() {
        Cart cart = new Cart(SINGLETON_CART_ID, BigDecimal.ZERO);
        CartItem cartItem = new CartItem(1L, SINGLETON_CART_ID, 101L, 2);
        Product product = new Product(101L, "Test Product", "imageUrl", "Desc", BigDecimal.valueOf(100));

        when(cartItemRepository.findByProductIdAndCartId(101L, SINGLETON_CART_ID)).thenReturn(Mono.just(cartItem));
        when(cartItemRepository.removeItemFromCart(SINGLETON_CART_ID, 101L)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.empty());
        when(productRepository.findById(cartItem.getProductId())).thenReturn(Mono.just(product));
        when(cartRepository.findById(SINGLETON_CART_ID)).thenReturn(Mono.just(cart));
        when(cartRepository.save(cart)).thenReturn(Mono.just(cart));

        StepVerifier.create(cartService.updateQuantity(101L, 0))
                .verifyComplete();

        verify(cartItemRepository, times(1)).removeItemFromCart(SINGLETON_CART_ID, 101L);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testUpdateQuantity_ProductNotFound_ShouldThrowException() {
        when(cartItemRepository.findByProductIdAndCartId(101L, SINGLETON_CART_ID)).thenReturn(Mono.empty());
        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.empty());

        StepVerifier.create(cartService.updateQuantity(101L, 3))
                .expectError(RuntimeException.class)
                .verify();

        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetTotalPrice_success() {
        CartItem cartItem1 = new CartItem(1L, SINGLETON_CART_ID, 101L, 2);
        CartItem cartItem2 = new CartItem(2L, SINGLETON_CART_ID, 102L, 1);
        Product product1 = new Product(101L, "Test Product 1", "imageUrl", "Desc", BigDecimal.valueOf(100));
        Product product2 = new Product(102L, "Test Product 2", "imageUrl", "Desc", BigDecimal.valueOf(50));

        when(cartItemRepository.findByCartId(SINGLETON_CART_ID)).thenReturn(Flux.just(cartItem1, cartItem2));
        when(productRepository.findById(101L)).thenReturn(Mono.just(product1));
        when(productRepository.findById(102L)).thenReturn(Mono.just(product2));

        StepVerifier.create(cartService.getTotalPrice())
                .expectNext(BigDecimal.valueOf(250))
                .verifyComplete();
    }

}
