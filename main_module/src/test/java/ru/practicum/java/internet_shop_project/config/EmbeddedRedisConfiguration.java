package ru.practicum.java.internet_shop_project.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import redis.embedded.RedisServer;

import java.io.IOException;

@TestConfiguration
public class EmbeddedRedisConfiguration {

    @Bean(destroyMethod = "stop")
    public RedisServer redisServer() throws IOException {
        RedisServer server = new RedisServer();
        server.start();
        return server;
    }

}
