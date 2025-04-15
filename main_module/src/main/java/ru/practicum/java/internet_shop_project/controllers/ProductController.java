package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.service.ProductService;
import ru.practicum.java.internet_shop_project.service.ViewAccessHelper;

import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    private final ViewAccessHelper viewAccessHelper;

    @GetMapping
    public Mono<Rendering> getProducts(
            Authentication authentication,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        return productService.getFilteredAndSortedProductsWithCaching(keyword, minPrice, maxPrice, page, size, sortBy, sortOrder)
                .collectList()
                .map(products -> Rendering.view("products")
                        .modelAttribute("products", products)
                        .modelAttribute("currentPage", page)
                        .modelAttribute("pageSize", size)
                        .modelAttribute("isAuthenticated", viewAccessHelper.isAuthenticated(authentication))
                        .build()
                );
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getProduct(@PathVariable("id") Long id, Authentication authentication) {
        return productService.getProductById(id)
                .map(product -> Rendering.view("product")
                        .modelAttribute("product", product)
                        .modelAttribute("isAuthenticated", viewAccessHelper.isAuthenticated(authentication))
                        .build()
                );
    }

}
