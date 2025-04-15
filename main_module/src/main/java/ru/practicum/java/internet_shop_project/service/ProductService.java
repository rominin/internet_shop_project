package ru.practicum.java.internet_shop_project.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.dto.ProductListItemDto;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final RedisCacheService redisCacheService;

    public Mono<Product> getProductById(Long id) {
        return redisCacheService.getProductById(id)
                .switchIfEmpty(
                        productRepository.findById(id)
                                .flatMap(product -> redisCacheService.cacheProduct(product).thenReturn(product))
                )
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")));
    }

    public Flux<Product> getFilteredAndSortedProducts(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortOrder) {

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : "";
        BigDecimal min = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        BigDecimal max = (maxPrice != null) ? maxPrice : BigDecimal.valueOf(1_000_000);

        return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(searchKeyword, min, max)
                .sort((p1, p2) -> {
                    int comparison;
                    switch (sortBy != null ? sortBy : "id") {
                        case "price":
                            comparison = p1.getPrice().compareTo(p2.getPrice());
                            break;
                        case "name":
                            comparison = p1.getName().compareToIgnoreCase(p2.getName());
                            break;
                        default:
                            comparison = p1.getId().compareTo(p2.getId());
                    }
                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .skip((long) page * size)
                .take(size);
    }

    public Flux<ProductListItemDto> getFilteredAndSortedProductsWithCaching(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortOrder) {

        String key = buildCacheKey(keyword, minPrice, maxPrice, page, size, sortBy, sortOrder);

        return redisCacheService.getCachedProductList(key)
                .flatMapMany(Flux::fromIterable)
                .switchIfEmpty(
                        getFilteredAndSortedProducts(keyword, minPrice, maxPrice, page, size, sortBy, sortOrder)
                                .map(p -> new ProductListItemDto(
                                        p.getId(),
                                        p.getName(),
                                        p.getDescription(),
                                        p.getPrice(),
                                        p.getImageUrl()
                                ))
                                .collectList()
                                .flatMap(list -> redisCacheService.cacheProductList(key, list).thenReturn(list))
                                .flatMapMany(Flux::fromIterable)
                );
    }

    private String buildCacheKey(String keyword, BigDecimal minPrice, BigDecimal maxPrice,
                                 int page, int size, String sortBy, String sortOrder) {
        return String.format("products:list:%s:%s:%s:%d:%d:%s:%s",
                keyword != null ? keyword : "all",
                minPrice != null ? minPrice.toPlainString() : "min",
                maxPrice != null ? maxPrice.toPlainString() : "max",
                page, size,
                sortBy != null ? sortBy : "id",
                sortOrder != null ? sortOrder : "asc"
        );
    }

}
