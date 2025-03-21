package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@SpringBootTest
public class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product(null, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00"));
        product2 = new Product(null, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00"));

        StepVerifier.create(productRepository.save(product1))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotNull();
                    product1.setId(saved.getId());
                })
                .verifyComplete();

        StepVerifier.create(productRepository.save(product2))
                .assertNext(saved -> {
                    assertThat(saved.getId()).isNotNull();
                    product2.setId(saved.getId());
                })
                .verifyComplete();
    }

    @Test
    void testSaveProduct_success() {
        Product newProduct = new Product(null, "Tablet", "someUrl", "Some Tablet", new BigDecimal("500.00"));

        StepVerifier.create(productRepository.save(newProduct))
                .assertNext(savedProduct -> {
                    assertThat(savedProduct).isNotNull();
                    assertThat(savedProduct.getId()).isNotNull();
                    assertThat(savedProduct.getName()).isEqualTo("Tablet");
                })
                .verifyComplete();
    }

    @Test
    void testFindById_success() {
        StepVerifier.create(productRepository.findById(product1.getId()))
                .assertNext(foundProduct -> {
                    assertThat(foundProduct).isNotNull();
                    assertThat(foundProduct.getName()).isEqualTo("Laptop");
                })
                .verifyComplete();
    }

    @Test
    void testFindById_notFound() {
        StepVerifier.create(productRepository.findById(999L))
                .expectNextCount(0)
                .verifyComplete();
    }

    @Test
    void testFindByNameContainingIgnoreCaseAndPriceBetween_success() {
        StepVerifier.create(productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                        "Phone", BigDecimal.ZERO, new BigDecimal("1000.00")
                ))
                .assertNext(foundProduct -> {
                    assertThat(foundProduct).isNotNull();
                    assertThat(foundProduct.getName()).isEqualTo("Phone");
                })
                .verifyComplete();
    }

}
