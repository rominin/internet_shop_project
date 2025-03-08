package ru.practicum.java.internet_shop_project.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.practicum.java.internet_shop_project.controllers.ProductController;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.service.ProductService;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProductController.class)
public class ProductControllerIntegrationMockTest {

    @MockitoBean
    private ProductService productService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetProducts_success() throws Exception {
        List<Product> products = List.of(
                new Product(1L, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00")),
                new Product(2L, "Phone", "someUrl", "Some Smartphone", new BigDecimal("800.00"))
        );

        Page<Product> page = new PageImpl<>(products);

        when(productService.getFilteredAndSortedProducts(any(), any(), any(), anyInt(), anyInt(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/products")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "name")
                        .param("sortOrder", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("products"))
                .andExpect(model().attributeExists("products", "currentPage", "pageSize", "totalPages"))
                .andExpect(model().attribute("products", products));
    }

    @Test
    void testGetProductById_success() throws Exception {
        Product product = new Product(1L, "Laptop", "someUrl", "Some Laptop", new BigDecimal("1500.00"));
        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("text/html;charset=UTF-8"))
                .andExpect(view().name("product"))
                .andExpect(model().attributeExists("product"))
                .andExpect(model().attribute("product", product));
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
                "text/csv", "name,imageUrl,description,price\nLaptop,testUrl,Some laptop,1500".getBytes());

        doNothing().when(productService).importProductsFromCsv(any());

        mockMvc.perform(multipart("/products/import")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/import"));
    }

    @Test
    void testImportProducts_failure() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile("file", "products.csv",
                "text/csv", "name,imageUrl,description,price\nLaptop,laptop.jpg,Some laptop,1500".getBytes());

        doThrow(new RuntimeException("Import error")).when(productService).importProductsFromCsv(any());

        mockMvc.perform(multipart("/products/import")
                        .file(mockFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/products/import"))
                .andExpect(flash().attributeExists("error"));
    }
}