package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductRepository productRepository;

    @BeforeEach
    void setUp() {
        List<Product> products = List.of(
                new Product(null, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")),
                new Product(null, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00"))
        );
        productRepository.saveAll(products);
    }

    @Test
    void testGetProducts_success() throws Exception {
        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products", "currentPage", "pageSize", "totalPages"));
    }

    @Test
    void testGetProductById_success() throws Exception {
        Product product = productRepository.findAll().getFirst();

        mockMvc.perform(get("/products/" + product.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", hasProperty("name", is("Laptop"))));
    }

    @Test
    void testShowImportPage_success() throws Exception {
        mockMvc.perform(get("/products/import"))
                .andExpect(status().isOk())
                .andExpect(view().name("import"));
    }

    @Test
    void testImportProducts_success() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "products.csv",
                "text/csv", "name,imageUrl,description,price\nTablet,tablet.jpg,Smart tablet,500".getBytes());

        mockMvc.perform(multipart("/products/import")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/import"));

        List<Product> products = productRepository.findAll().stream().filter(p -> p.getName().equals("Tablet")).toList();
        assertThat(products).isNotEmpty();
        assertThat(products.getFirst().getPrice()).isEqualByComparingTo(new BigDecimal("500"));
    }

}
