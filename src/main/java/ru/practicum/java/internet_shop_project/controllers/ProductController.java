package ru.practicum.java.internet_shop_project.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.ProductService;

import java.math.BigDecimal;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public String getProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "asc") String sortOrder,
            Model model) {

        Page<Product> products = productService.getFilteredAndSortedProducts(
                keyword, minPrice, maxPrice, page, size, sortBy, sortOrder
        );

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("pageSize", size);
        model.addAttribute("totalPages", products.getTotalPages());

        return "products";
    }

    @GetMapping("/{id}")
    public String getProduct(@PathVariable("id") Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product";
    }

    @GetMapping("/import")
    public String showImportPage() {
        return "import";
    }

    @PostMapping(path = "/import", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public String importProducts(@RequestPart MultipartFile file, RedirectAttributes redirectAttributes) {
        try {
            productService.importProductsFromCsv(file);
            redirectAttributes.addFlashAttribute("success", "Products imported successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error while products import: " + e.getMessage());
        }
         return "redirect:/products/import";
    }

}
