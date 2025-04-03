package ru.practicum.java.internet_shop_project.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.practicum.java.internet_shop_project.dto.ProductListItemDto;
import ru.practicum.java.internet_shop_project.entity.Product;

import java.time.Duration;
import java.util.List;

@Service
public class RedisCacheService {

    private final ReactiveRedisTemplate<String, Product> reactiveRedisTemplate;
    private final ReactiveRedisTemplate<String, String> redisTemplateForList;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisCacheService(ReactiveRedisTemplate<String, Product> reactiveRedisTemplate,
                             ReactiveRedisTemplate<String, String> redisTemplateForList,
                             ObjectMapper objectMapper,
                             @Value("${spring.cache-ttl:3}") int ttlMinutes) {
        this.reactiveRedisTemplate = reactiveRedisTemplate;
        this.redisTemplateForList = redisTemplateForList;
        this.objectMapper = objectMapper;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    public Mono<Product> getProductById(Long id) {
        return reactiveRedisTemplate.opsForValue().get("product:" + id);
    }

    public Mono<Void> cacheProduct(Product product) {
        return reactiveRedisTemplate.opsForValue()
                .set("product:" + product.getId(), product, ttl)
                .then();
    }

    public Mono<List<ProductListItemDto>> getCachedProductList(String key) {
        return redisTemplateForList.opsForValue().get(key)
                .flatMap(json -> {
                    try {
                        List<ProductListItemDto> list = objectMapper.readValue(
                                json,
                                new TypeReference<>() {}
                        );
                        return Mono.just(list);
                    } catch (JsonProcessingException e) {
                        return Mono.error(e);
                    }
                });
    }

    public Mono<Void> cacheProductList(String key, List<ProductListItemDto> list) {
        try {
            String json = objectMapper.writeValueAsString(list);
            return redisTemplateForList.opsForValue()
                    .set(key, json, ttl)
                    .then();
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

}
