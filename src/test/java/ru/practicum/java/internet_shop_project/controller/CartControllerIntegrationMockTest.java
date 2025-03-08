package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.java.internet_shop_project.controllers.CartController;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.service.CartService;
import ru.practicum.java.internet_shop_project.service.OrderService;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerIntegrationMockTest {

    @MockitoBean
    private CartService cartService;

    @MockitoBean
    private OrderService orderService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCart_success() throws Exception {
        Cart cart = new Cart();
        cart.setTotalPrice(BigDecimal.ZERO);
        cart.setCartItems(new ArrayList<>());
        when(cartService.getCart()).thenReturn(cart);

        mockMvc.perform(get("/cart"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("cart"))
                .andExpect(model().attribute("cart", cart));
    }

    @Test
    void testAddToCart_success() throws Exception {
        Long productId = 1L;
        int quantity = 2;

        mockMvc.perform(post("/cart/add")
                        .param("productId", productId.toString())
                        .param("quantity", String.valueOf(quantity)))
                .andExpect(status().is3xxRedirection());

        verify(cartService, times(1)).addProductToCart(productId, quantity);
    }

    @Test
    void testRemoveFromCart_success() throws Exception {
        Long productId = 1L;

        mockMvc.perform(post("/cart/remove")
                        .param("productId", productId.toString()))
                .andExpect(status().is3xxRedirection());

        verify(cartService, times(1)).removeProductFromCart(productId);
    }

    @Test
    void testUpdateCartItem_success() throws Exception {
        Long productId = 1L;
        int newQuantity = 5;

        mockMvc.perform(post("/cart/update")
                        .param("productId", productId.toString())
                        .param("quantity", String.valueOf(newQuantity)))
                .andExpect(status().is3xxRedirection());

        verify(cartService, times(1)).updateQuantity(productId, newQuantity);
    }

//    @Test
//    void testAddToCart_invalidQuantity_shouldFail() throws Exception {
//        Long productId = 1L;
//        int invalidQuantity = 0;
//
//        mockMvc.perform(post("/cart/add")
//                        .param("productId", productId.toString())
//                        .param("quantity", String.valueOf(invalidQuantity)))
//                .andExpect(status().is3xxRedirection());
//
//        verify(cartService, never()).addProductToCart(anyLong(), eq(invalidQuantity));
//    }
//
//    @Test
//    void testUpdateCartItem_invalidQuantity_shouldFail() throws Exception {
//        Long productId = 1L;
//        int invalidQuantity = -1;
//
//        mockMvc.perform(post("/cart/update")
//                        .param("productId", productId.toString())
//                        .param("quantity", String.valueOf(invalidQuantity)))
//                .andExpect(status().is3xxRedirection());
//
//        verify(cartService, never()).updateQuantity(anyLong(), eq(invalidQuantity));
//    }

}
