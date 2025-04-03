package ru.practicum.java.internet_shop_project.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductListItemDto {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;
}