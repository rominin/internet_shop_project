package ru.practicum.java.internet_shop_project.service;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = {CartService.class})
public class CartServiceUnitTest {

    @MockitoBean
    private CartRepository cartRepository;

    @MockitoBean
    private CartItemRepository cartItemRepository;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private CartService cartService;

    @Test
    void testGetCart_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalPrice(BigDecimal.ZERO);

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));

        Cart foundCart = cartService.getCart();

        assertNotNull(foundCart);
        assertEquals(cart.getId(), foundCart.getId());
        verify(cartRepository, times(1)).findSingletonCart();
    }

    @Test
    void testGetCartItems_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        CartItem item1 = new CartItem(1L, cart, new Product(), 2);
        CartItem item2 = new CartItem(2L, cart, new Product(), 3);

        List<CartItem> mockItems = List.of(item1, item2);

        when(cartItemRepository.findInSingletonCart()).thenReturn(mockItems);

        List<CartItem> result = cartService.getCartItems();

        assertEquals(2, result.size());
        assertEquals(item1, result.get(0));
        assertEquals(item2, result.get(1));

        verify(cartItemRepository, times(1)).findInSingletonCart();
    }

    @Test
    void testAddProductToCart_NewProduct_ShouldBeAdded() {
        Cart cart = new Cart();
        Product product = new Product(1L, "Test Product", "imageUrl", "Description", BigDecimal.valueOf(100));

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findInSingletonCartByProductId(product.getId())).thenReturn(Optional.empty());

        cartService.addProductToCart(product.getId(), 2);

        verify(cartItemRepository, times(1)).save(any(CartItem.class));
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testAddProductToCart_ExistingProduct_ShouldIncreaseQuantity() {
        Cart cart = new Cart();
        Product product = new Product(1L, "Test Product", "imageUrl", "Description", BigDecimal.valueOf(100));
        CartItem cartItem = new CartItem(1L, cart, product, 1);

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));
        when(productRepository.findById(product.getId())).thenReturn(Optional.of(product));
        when(cartItemRepository.findInSingletonCartByProductId(product.getId())).thenReturn(Optional.of(cartItem));

        cartService.addProductToCart(product.getId(), 2);

        assertEquals(3, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
    }

    @Test
    void testRemoveProductFromCart_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalPrice(BigDecimal.ZERO);

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));

        doNothing().when(cartItemRepository).removeItemFromSingletonCart(1L);

        cartService.removeProductFromCart(1L);

        verify(cartItemRepository, times(1)).removeItemFromSingletonCart(1L);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testUpdateQuantity_ShouldUpdateQuantity() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalPrice(BigDecimal.ZERO);

        Product product = new Product(1L, "Test Product", "imageUrl","Description", BigDecimal.valueOf(100));
        CartItem cartItem = new CartItem(1L, cart, product, 1);

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));

        when(cartItemRepository.findInSingletonCartByProductId(product.getId())).thenReturn(Optional.of(cartItem));

        cartService.updateQuantity(product.getId(), 5);

        assertEquals(5, cartItem.getQuantity());
        verify(cartItemRepository, times(1)).save(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testUpdateQuantity_ToZero_ShouldRemoveProduct() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalPrice(BigDecimal.ZERO);

        Product product = new Product(1L, "Test Product", "imageUrl", "Description", BigDecimal.valueOf(100));
        CartItem cartItem = new CartItem(1L, cart, product, 2);

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));
        when(cartItemRepository.findInSingletonCartByProductId(product.getId())).thenReturn(Optional.of(cartItem));
        doNothing().when(cartItemRepository).delete(cartItem);

        cartService.updateQuantity(product.getId(), 0);

        verify(cartItemRepository, times(1)).delete(cartItem);
        verify(cartRepository, times(1)).save(cart);
    }

    @Test
    void testUpdateQuantity_ProductNotFound_ShouldThrowException() {
        when(cartItemRepository.findInSingletonCartByProductId(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> cartService.updateQuantity(1L, 3));

        verify(cartItemRepository, never()).save(any(CartItem.class));
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetTotalPrice_success() {
        Cart cart = new Cart();
        Product product1 = new Product(1L, "Test Product 1", "imageUrl", "Desc", BigDecimal.valueOf(100));
        Product product2 = new Product(2L, "Test Product 2", "imageUrl", "Desc", BigDecimal.valueOf(50));

        CartItem cartItem1 = new CartItem(1L, cart, product1, 2);
        CartItem cartItem2 = new CartItem(2L, cart, product2, 1);

        when(cartItemRepository.findInSingletonCart()).thenReturn(List.of(cartItem1, cartItem2));

        BigDecimal totalPrice = cartService.getTotalPrice();

        assertEquals(BigDecimal.valueOf(250), totalPrice);
    }

    @Test
    void testUpdateCartTotalPrice_success() {
        Cart cart = new Cart();
        cart.setId(1L);
        cart.setTotalPrice(BigDecimal.ZERO);

        Product product1 = new Product();
        product1.setPrice(new BigDecimal("100.00"));

        Product product2 = new Product();
        product2.setPrice(new BigDecimal("150.00"));

        CartItem cartItem1 = new CartItem(1L, cart, product1, 1);
        CartItem cartItem2 = new CartItem(2L, cart, product2, 1);

        List<CartItem> cartItems = List.of(cartItem1, cartItem2);
        BigDecimal expectedTotalPrice = new BigDecimal("250.00");

        when(cartRepository.findSingletonCart()).thenReturn(Optional.of(cart));
        when(cartItemRepository.findInSingletonCart()).thenReturn(cartItems);

        cartService.updateCartTotalPrice();

        assertEquals(expectedTotalPrice, cart.getTotalPrice());
        verify(cartRepository, times(1)).save(cart);
    }

}
