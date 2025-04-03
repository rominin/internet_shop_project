package ru.practicum.java.internet_shop_project.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import ru.practicum.java.internet_shop_project.entity.Product;

@Configuration
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Product> reactiveRedisTemplate(ReactiveRedisConnectionFactory redisConnectionFactory) {
        RedisSerializationContext<String, Product> serializationContext = RedisSerializationContext
                .<String, Product>newSerializationContext(new StringRedisSerializer())
                .value(new Jackson2JsonRedisSerializer<>(Product.class))
                .build();
        return new ReactiveRedisTemplate<>(redisConnectionFactory, serializationContext);
    }

    @Bean
    public ReactiveRedisTemplate<String, String> redisTemplateForList(ReactiveRedisConnectionFactory factory) {
        RedisSerializationContext<String, String> context = RedisSerializationContext
                .<String, String>newSerializationContext(RedisSerializer.string())
                .value(RedisSerializer.string())
                .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

}
