package ru.practicum.java.internet_shop_project.service;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.dto.ProductCsvDto;
import ru.practicum.java.internet_shop_project.entity.Product;
import ru.practicum.java.internet_shop_project.repository.ProductRepository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Mono<Product> getProductById(Long id) {
        return productRepository.findById(id)
                .switchIfEmpty(Mono.error(new RuntimeException("Product not found")));
    }

    public Flux<Product> getFilteredAndSortedProducts(
            String keyword, BigDecimal minPrice, BigDecimal maxPrice,
            int page, int size, String sortBy, String sortOrder) {

        String searchKeyword = (keyword != null && !keyword.isBlank()) ? keyword : "";
        BigDecimal min = (minPrice != null) ? minPrice : BigDecimal.ZERO;
        BigDecimal max = (maxPrice != null) ? maxPrice : BigDecimal.valueOf(1_000_000);

        return productRepository.findByNameContainingIgnoreCaseAndPriceBetween(searchKeyword, min, max)
                .sort((p1, p2) -> {
                    int comparison;
                    switch (sortBy != null ? sortBy : "id") {
                        case "price":
                            comparison = p1.getPrice().compareTo(p2.getPrice());
                            break;
                        case "name":
                            comparison = p1.getName().compareToIgnoreCase(p2.getName());
                            break;
                        default:
                            comparison = p1.getId().compareTo(p2.getId());
                    }
                    return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
                })
                .skip((long) page * size)
                .take(size);
    }

    public Mono<Void> importProductsFromCsv(FilePart filePart) {
        return filePart.content()
                .reduce(new StringBuilder(), (acc, buffer) -> acc.append(StandardCharsets.UTF_8.decode(buffer.asByteBuffer())))
                .map(StringBuilder::toString)
                .flatMap(csvContent -> Mono.fromCallable(() -> parseCsv(csvContent)))
                .flatMapMany(Flux::fromIterable)
                .map(ProductCsvDto::toEntity)
                .flatMap(productRepository::save)
                .then();
    }

    private List<ProductCsvDto> parseCsv(String csvContent) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                new java.io.ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8))))) {

            CsvToBean<ProductCsvDto> csvToBean = new CsvToBeanBuilder<ProductCsvDto>(reader)
                    .withType(ProductCsvDto.class)
                    .withIgnoreLeadingWhiteSpace(true)
                    .build();

            return csvToBean.parse();

        } catch (Exception e) {
            throw new RuntimeException("Error parsing CSV", e);
        }
    }

}
