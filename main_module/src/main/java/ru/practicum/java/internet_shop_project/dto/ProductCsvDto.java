package ru.practicum.java.internet_shop_project.dto;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;
import lombok.Setter;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductCsvDto {

    @CsvBindByName(column = "name")
    private String name;

    @CsvBindByName(column = "imageUrl")
    private String imageUrl;

    @CsvBindByName(column = "description")
    private String description;

    @CsvBindByName(column = "price")
    private BigDecimal price;

    public Product toEntity() {
        return new Product(null, name, imageUrl, description, price);
    }

}
