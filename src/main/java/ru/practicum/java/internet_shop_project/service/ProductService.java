package ru.practicum.java.internet_shop_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts(int page, int size, String sortBy, String sortOrder) {
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).getContent();
    }

    public List<Product> searchProductsByNameContaining(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<Product> getFilteredAndSortedProducts(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortOrder) {

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : "%"; // или просто "" ?
        BigDecimal min = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        BigDecimal max = (minPrice != null) ? maxPrice : BigDecimal.valueOf(1_000_000);

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                searchKeyword, min, max, pageable
        );

        return productPage.getContent();
    }

}
