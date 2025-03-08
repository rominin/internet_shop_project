package ru.practicum.java.internet_shop_project.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    Optional<Product> findById(Long id);

    Page<Product> findByNameContainingIgnoreCaseAndPriceBetween(
            String name, BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable
    );

}
