package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.service.ProductService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public Mono<Rendering> getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder) {

        return productService.getFilteredAndSortedProducts(keyword, minPrice, maxPrice, page, size, sortBy, sortOrder)
                .collectList()
                .map(products -> Rendering.view("products")
                        .modelAttribute("products", products)
                        .modelAttribute("currentPage", page)
                        .modelAttribute("pageSize", size)
                        .build()
                );
    }

    @GetMapping("/{id}")
    public Mono<Rendering> getProduct(@PathVariable("id") Long id) {
        return productService.getProductById(id)
                .map(product -> Rendering.view("product")
                        .modelAttribute("product", product)
                        .build()
                );
    }

    @GetMapping("/import")
    public Mono<Rendering> showImportPage() {
        return Mono.just(Rendering.view("import").build());
    }

    @PostMapping("/import")
    public Mono<Rendering> importProducts(@RequestPart("file") FilePart filePart) {
        return productService.importProductsFromCsv(filePart)
                .then(Mono.just(
                        Rendering.view("import")
                                .modelAttribute("success", "Products imported successfully!")
                                .build()))
                .onErrorResume(e -> Mono.just(
                        Rendering.view("import")
                                .modelAttribute("error", "Error while importing products: " + e.getMessage())
                                .build()));
    }

}
