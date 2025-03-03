package ru.practicum.java.internet_shop_project.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @Test
    @Transactional
    void testGetProductById_success() {
        Product savedProduct = new Product(null, "Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));
        productRepository.save(savedProduct);

        Product foundProduct = productService.getProductById(savedProduct.getId());

        assertThat(foundProduct).isNotNull();
        assertThat(foundProduct.getId()).isEqualTo(savedProduct.getId());
        assertThat(foundProduct.getName()).isEqualTo("Laptop");
    }

    @Test
    void testGetProductById_failure() {
        assertThrows(RuntimeException.class, () -> productService.getProductById(999L));
    }

    @Test
    @Transactional
    void testGetFilteredAndSortedProducts_success() {
        Product product1 = new Product(null, "Laptop", "testUrl", "Some laptop", BigDecimal.valueOf(1500));
        Product product2 = new Product(null, "Phone", "testUrl", "Some phone", BigDecimal.valueOf(750));

        productRepository.save(product1);
        productRepository.save(product2);

        Page<Product> products = productService.getFilteredAndSortedProducts(
                "", BigDecimal.valueOf(100), BigDecimal.valueOf(1000), 0, 10, "name", "asc"
        );

        assertThat(products.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(products.getContent().getFirst().getName()).isEqualTo("Phone");
    }

    @Test
    @Transactional
    void testImportProductsFromCsv_success() throws Exception {
        String csvData = "name,imageUrl,description,price\n" +
                "Watch,watch.jpg,Smartwatch,250\n" +
                "Camera,camera.jpg,DSLR Camera,800\n";

        MultipartFile csvFile = new MockMultipartFile(
                "file", "products.csv", "text/csv", csvData.getBytes()
        );

        productService.importProductsFromCsv(csvFile);

        Optional<Product> watch = productRepository.findAll().stream().filter(p -> p.getName().equals("Watch")).findFirst();
        Optional<Product> camera = productRepository.findAll().stream().filter(p -> p.getName().equals("Camera")).findFirst();

        assertThat(watch).isPresent();
        assertThat(camera).isPresent();
        assertThat(watch.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(250));
        assertThat(camera.get().getPrice()).isEqualByComparingTo(BigDecimal.valueOf(800));
    }

}
