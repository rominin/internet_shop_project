package ru.practicum.java.internet_shop_project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.practicum.java.internet_shop_project.dto.ProductListItemDto;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = RedisCacheService.class)
public class RedisCacheServiceUnitTest {

    @MockitoBean
    private ReactiveRedisTemplate<String, Product> reactiveRedisTemplate;

    @MockitoBean
    private ReactiveRedisTemplate<String, String> redisTemplateForList;

    @MockitoBean
    private ObjectMapper objectMapper;

    @Autowired
    private RedisCacheService redisCacheService;

    @Test
    void testGetProductById_success() {
        Product product = new Product(145L, "Some product", "url", "Desc", BigDecimal.valueOf(1000));

        ReactiveValueOperations<String, Product> valueOpsMock = mock(ReactiveValueOperations.class);
        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOpsMock);
        when(reactiveRedisTemplate.opsForValue().get("product:145")).thenReturn(Mono.just(product));

        StepVerifier.create(redisCacheService.getProductById(145L))
                .expectNextMatches(p -> p.getId().equals(145L) && p.getName().equals("Some product"))
                .verifyComplete();
    }

    @Test
    void testCacheProduct_success() {
        Product product = new Product(245L, "Phone just phone", "url", "Desc", BigDecimal.valueOf(500));
        ReactiveValueOperations<String, Product> valueOpsMock = mock(ReactiveValueOperations.class);

        when(reactiveRedisTemplate.opsForValue()).thenReturn(valueOpsMock);
        when(valueOpsMock.set(eq("product:245"), eq(product), eq(Duration.ofMinutes(3))))
                .thenReturn(Mono.just(true));

        StepVerifier.create(redisCacheService.cacheProduct(product))
                .verifyComplete();

        verify(valueOpsMock, times(1))
                .set("product:245", product, Duration.ofMinutes(3));
    }

    @Test
    void testCacheProductList_success() throws JsonProcessingException {
        List<ProductListItemDto> list = List.of(
                new ProductListItemDto(348L, "Tablet just tablet", "desc", BigDecimal.valueOf(300), "url")
        );
        String expectedJson = objectMapper.writeValueAsString(list);

        ReactiveValueOperations<String, String> listOpsMock = mock(ReactiveValueOperations.class);
        when(redisTemplateForList.opsForValue()).thenReturn(listOpsMock);
        when(listOpsMock.set(eq("products:list:cache"), eq(expectedJson), eq(Duration.ofMinutes(3))))
                .thenReturn(Mono.just(true));

        StepVerifier.create(redisCacheService.cacheProductList("products:list:cache", list))
                .verifyComplete();

        verify(listOpsMock).set("products:list:cache", expectedJson, Duration.ofMinutes(3));
    }

}
