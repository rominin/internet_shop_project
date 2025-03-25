package ru.practicum.java.internet_shop_project.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import ru.practicum.java.internet_shop_project.entity.Order;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, Long> {
}
