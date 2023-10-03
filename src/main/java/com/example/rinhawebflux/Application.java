package com.example.rinhawebflux;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import reactor.core.publisher.Sinks;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public Sinks.Many<Pessoa> sink() {
        return Sinks.many().multicast().onBackpressureBuffer();
    }

    @Bean
    @Primary
    ReactiveRedisOperations<String, String> redisOperations(ReactiveRedisConnectionFactory connectionFactory) {
        Jackson2JsonRedisSerializer<String> serializer = new Jackson2JsonRedisSerializer<>(String.class);

        RedisSerializationContext.RedisSerializationContextBuilder<String, String> builder =
                RedisSerializationContext.newSerializationContext(new StringRedisSerializer());

        RedisSerializationContext<String, String> context = builder.value(serializer).build();

        return new ReactiveRedisTemplate<>(connectionFactory, context);
    }
}
