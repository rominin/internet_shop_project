package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.config.EmbeddedRedisConfiguration;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@Import(EmbeddedRedisConfiguration.class)
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    void testGetProductById_success() {
        Product savedProduct = new Product(null, "New Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));

        StepVerifier.create(productRepository.save(savedProduct)
                        .flatMap(saved -> productService.getProductById(saved.getId())))
                .assertNext(foundProduct -> {
                    assertThat(foundProduct).isNotNull();
                    assertThat(foundProduct.getId()).isEqualTo(savedProduct.getId());
                    assertThat(foundProduct.getName()).isEqualTo("New Laptop");
                })
                .verifyComplete();
    }

    @Test
    void testGetProductById_failure() {
        StepVerifier.create(productService.getProductById(999L))
                .expectErrorMatches(throwable -> throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Product not found"))
                .verify();
    }

    @Test
    void testGetFilteredAndSortedProducts_success() {
        Product product1 = new Product(null, "Some Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));
        Product product2 = new Product(null, "Some Phone", "testUrl", "Some phone", BigDecimal.valueOf(760));

        StepVerifier.create(productRepository.saveAll(Flux.just(product1, product2)).thenMany(
                        productService.getFilteredAndSortedProducts(
                                "", BigDecimal.valueOf(750), BigDecimal.valueOf(770), 0, 10, "name", "asc")))
                .assertNext(foundProduct -> assertThat(foundProduct.getName()).isEqualTo("Some Phone"))
                .verifyComplete();
    }

}
