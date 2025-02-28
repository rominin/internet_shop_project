package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(1L);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());
        assertEquals(BigDecimal.valueOf(1500), result.getPrice());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void testGetProductById_WhenProductDoesNotExist() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> productService.getProductById(999L));
        assertEquals("Product not found", exception.getMessage());

        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void testGetFilteredAndSortedProducts_success() {
        Product product1 = new Product(1L, "Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));
        Product product2 = new Product(2L, "Phone", "testUrl", "Some phone", BigDecimal.valueOf(750));

        when(productRepository.findByNameContainingIgnoreCaseAndPriceBetween(anyString(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(product1, product2)));

        Page<Product> results = productService.getFilteredAndSortedProducts(
                null, BigDecimal.ZERO, BigDecimal.valueOf(2000), 0, 10, "price", "asc"
        );

        assertNotNull(results);
        assertEquals(2, results.getContent().size());
        assertEquals("Laptop", results.getContent().getFirst().getName());

        verify(productRepository, times(1))
                .findByNameContainingIgnoreCaseAndPriceBetween(anyString(), any(), any(), any());
    }

}
