package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.practicum.java.internet_shop_project.entity.Cart;
import ru.practicum.java.internet_shop_project.service.CartService;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public String getCart(Model model) {
        Cart cart = cartService.getCart();
        model.addAttribute("cart", cart);
        return "cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Long productId, @RequestParam Integer quantity,
                            @RequestHeader(value = "Referer", required = false) String referer) {
        cartService.addProductToCart(productId, quantity);
        return referer != null ? "redirect:" + referer : "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Long productId,
                                 @RequestHeader(value = "Referer", required = false) String referer) {
        cartService.removeProductFromCart(productId);
        return referer != null ? "redirect:" + referer : "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCartItem(@RequestParam Long productId, @RequestParam Integer quantity,
                                 @RequestHeader(value = "Referer", required = false) String referer) {
        cartService.updateQuantity(productId, quantity);
        return referer != null ? "redirect:" + referer : "redirect:/cart";
    }

}
