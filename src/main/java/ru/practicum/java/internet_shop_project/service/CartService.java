package ru.practicum.java.internet_shop_project.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.entity.CartItem;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.CartItemRepository;
import ru.practicum.java.internet_shop_project.repository.CartRepository;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public Cart getCart() {
        return cartRepository.findSingletonCart().orElseGet(() -> {
                    Cart cart = new Cart();
                    return cartRepository.save(cart);
                }
        );
    }

    @Transactional
    public List<CartItem> getCartItems() {
        return cartItemRepository.findInSingletonCart();
    }

    @Transactional
    public void addProductToCart(Long productId, Integer quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Product quantity must be greater than 0");
        }

        Cart cart = getCart();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        Optional<CartItem> existingItem = cartItemRepository.findInSingletonCartByProductId(productId);

        if (existingItem.isPresent()) {
            CartItem cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + quantity);
            cartItemRepository.save(cartItem);
        } else {
            CartItem newCartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();
            cartItemRepository.save(newCartItem);
        }

        updateCartTotalPrice();

    }

    @Transactional
    public void removeProductFromCart(Long productId) {
        cartItemRepository.removeItemFromSingletonCart(productId);

        updateCartTotalPrice();
    }

    @Transactional
    public void updateQuantity(Long productId, Integer quantity) {
        if (quantity < 0) {
            throw new IllegalArgumentException("Product quantity must be greater than 0");
        }

        CartItem cartItem = cartItemRepository.findInSingletonCartByProductId(productId)
                .orElseThrow(() -> new EntityNotFoundException("Product not found"));

        if (quantity == 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
        }

        updateCartTotalPrice();
    }

    @Transactional(readOnly = true)
    public BigDecimal getTotalPrice() {
        return getCartItems().stream()
                .map(item -> item.getProduct().getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional
    public void updateCartTotalPrice() {
        Cart cart = getCart();
        BigDecimal totalPrice = getTotalPrice();
        cart.setTotalPrice(totalPrice);
        cartRepository.save(cart);
    }

}
