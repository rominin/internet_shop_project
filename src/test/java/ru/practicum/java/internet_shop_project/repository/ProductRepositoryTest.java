package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();

        product1 = productRepository.save(new Product(null, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")));
        product2 = productRepository.save(new Product(null, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00")));
    }

    @Test
    void testSaveProduct_success() {
        assertThat(product1).isNotNull();
        assertThat(product1.getId()).isNotNull();
        assertThat(product1.getName()).isEqualTo("Laptop");
    }

    @Test
    void testFindById_success() {
        Optional<Product> foundProduct = productRepository.findById(product1.getId());

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void testFindById_notFound() {
        Optional<Product> foundProduct = productRepository.findById(999L);

        assertThat(foundProduct).isEmpty();
    }

    @Test
    void testFindByNameContainingIgnoreCaseAndPriceBetween_success() {
        var result = productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                "Phone", BigDecimal.ZERO, new BigDecimal("1000.00"), null
        );

        assertThat(result).isNotEmpty();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Phone");
    }

}
