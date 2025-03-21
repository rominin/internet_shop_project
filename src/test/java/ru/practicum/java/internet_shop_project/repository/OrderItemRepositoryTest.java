package ru.practicum.java.internet_shop_project.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.entity.Order;
import ru.practicum.java.internet_shop_project.entity.OrderItem;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OrderItemRepositoryTest {

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    private Long orderId;

    @BeforeEach
    void setUp() {
        Order order = new Order(null, new BigDecimal("2300.00"));

        orderId = orderRepository.save(order)
                .map(Order::getId)
                .block();

    }

    @Test
    void testFindByOrderId_success() {
        Product product1 = productRepository.save(new Product(null, "Laptop", "imageUrl", "Some Laptop", new BigDecimal("1500.00")))
                .block();
        Product product2 = productRepository.save(new Product(null, "Phone", "imageUrl", "Some Smartphone", new BigDecimal("800.00")))
                .block();

        OrderItem orderItem1 = new OrderItem(null, orderId, product1.getId(), 2);
        OrderItem orderItem2 = new OrderItem(null, orderId, product2.getId(), 1);

        orderItemRepository.save(orderItem1).block();
        orderItemRepository.save(orderItem2).block();

        StepVerifier.create(orderItemRepository.findByOrderId(orderId))
                .expectNextMatches(item -> item.getProductId().equals(product1.getId()) && item.getQuantity() == 2)
                .expectNextMatches(item -> item.getProductId().equals(product2.getId()) && item.getQuantity() == 1)
                .verifyComplete();
    }

    @Test
    void testFindByOrderId_notFound() {
        StepVerifier.create(orderItemRepository.findByOrderId(999L))
                .expectNextCount(0)
                .verifyComplete();
    }

}
