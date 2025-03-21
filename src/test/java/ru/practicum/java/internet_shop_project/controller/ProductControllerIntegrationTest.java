package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test-webflux")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ProductControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        List<Product> products = List.of(
                new Product(null, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")),
                new Product(null, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00"))
        );
        productRepository.saveAll(products).collectList().block();
    }

    @Test
    void testGetProducts_success() {
        webTestClient.get()
                .uri("/products?page=0&size=10&sortBy=name&sortOrder=asc")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull()
                            .contains("Laptop", "Phone")
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testGetProductById_success() {
        Product product = productRepository.findAll().blockFirst();

        webTestClient.get()
                .uri("/products/" + product.getId())
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .consumeWith(response -> {
                    String responseBody = response.getResponseBody();
                    assertThat(responseBody).isNotNull()
                            .contains(product.getName())
                            .contains(product.getPrice().toString())
                            .contains("<html>", "<body>");
                });
    }

    @Test
    void testShowImportPage_success() {
        webTestClient.get()
                .uri("/products/import")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("<title>Импорт товаров</title>"));
    }

    @Test
    void testImportProducts_success() {
        byte[] csvBytes = "name,imageUrl,description,price\nTablet,tablet.jpg,Smart tablet,500".getBytes(StandardCharsets.UTF_8);
        ByteArrayResource csvResource = new ByteArrayResource(csvBytes) {
            @Override
            public String getFilename() {
                return "products.csv";
            }
        };

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("file", csvResource)
                .header("Content-Disposition", "form-data; name=file; filename=products.csv")
                .contentType(MediaType.TEXT_PLAIN);

        webTestClient.post()
                .uri("/products/import")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().isOk();

        List<Product> products = productRepository.findAll().collectList().block();
        assertThat(products).isNotNull();
        assertThat(products.stream().anyMatch(p -> "Tablet".equals(p.getName()) && p.getPrice().compareTo(new BigDecimal("500")) == 0))
                .isTrue();
    }

}
