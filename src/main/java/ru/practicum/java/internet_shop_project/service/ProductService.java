package ru.practicum.java.internet_shop_project.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.practicum.java.internet_shop_project.dto.ProductCsvDto;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public List<Product> getAllProducts(int page, int size, String sortBy, String sortOrder) {
        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return productRepository.findAll(pageable).getContent();
    }

    public List<Product> searchProductsByNameContaining(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    public List<Product> getProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public Page<Product> getFilteredAndSortedProducts(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortOrder) {

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : "";
        BigDecimal min = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        BigDecimal max = (maxPrice != null) ? maxPrice : BigDecimal.valueOf(1_000_000);

        String sortField = (sortBy != null && !sortBy.isBlank()) ? sortBy : "id";
        Sort sort = "desc".equalsIgnoreCase(sortOrder) ? Sort.by(sortField).descending() : Sort.by(sortField).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Product> productPage = productRepository.findByNameContainingIgnoreCaseAndPriceBetween(
                searchKeyword, min, max, pageable
        );

        return productPage;
    }

    public void importProductsFromCsv(MultipartFile file) throws Exception {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        try (InputStreamReader inputStreamReader = new InputStreamReader(file.getInputStream())) {
            CsvToBean<ProductCsvDto> csvToBean = new CsvToBeanBuilder<ProductCsvDto>(inputStreamReader)
                    .withType(ProductCsvDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            List<ProductCsvDto> productsDto = csvToBean.parse();
            List<Product> products = productsDto.stream().map(ProductCsvDto::toEntity).toList();
            productRepository.saveAll(products);
        }
    }

}
