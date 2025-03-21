package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;

import static org.mockito.Mockito.*;

@SpringBootTest(classes = ProductService.class)
public class ProductServiceUnitTest {

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private ProductService productService;

    @Test
    void testGetProductById_WhenProductExists() {
        Product product = new Product(1L, "Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));

        when(productRepository.findById(1L)).thenReturn(Mono.just(product));

        StepVerifier.create(productService.getProductById(1L))
                .expectNextMatches(p -> p.getName().equals("Laptop") && p.getPrice().compareTo(BigDecimal.valueOf(1500)) == 0)
                .verifyComplete();

        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_WhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Mono.empty());

        StepVerifier.create(productService.getProductById(999L))
                .expectErrorMatches(ex -> ex instanceof RuntimeException && ex.getMessage().equals("Product not found"))
                .verify();

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testGetFilteredAndSortedProducts_Success() {
        Product product1 = new Product(1L, "Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));
        Product product2 = new Product(2L, "Phone", "testUrl", "Some phone", BigDecimal.valueOf(750));

        when(productRepository.findByNameContainingIgnoreCaseAndPriceBetween(any(), any(), any()))
                .thenReturn(Flux.just(product1, product2));

        StepVerifier.create(productService.getFilteredAndSortedProducts(null, BigDecimal.ZERO, BigDecimal.valueOf(2000), 0, 10, "price", "asc"))
                .expectNext(product2, product1)
                .verifyComplete();

        verify(productRepository, times(1))
                .findByNameContainingIgnoreCaseAndPriceBetween(any(), any(), any());
    }

}
