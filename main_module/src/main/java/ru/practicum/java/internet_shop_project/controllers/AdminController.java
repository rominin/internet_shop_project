package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.service.ProductService;

@Controller
@RequestMapping("/import")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Mono<Rendering> showImportPage() {
        return Mono.just(Rendering.view("import").build());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
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
