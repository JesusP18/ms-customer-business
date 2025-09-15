package com.customer.business.config;

import com.customer.business.model.entity.Customer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public ReactiveRedisTemplate<String, Customer> customerReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Customer> serializer =
                new Jackson2JsonRedisSerializer<>(Customer.class);
        RedisSerializationContext<String, Customer> context =
                RedisSerializationContext.<String, Customer>newSerializationContext(
                                new StringRedisSerializer())
                        .value(serializer)
                        .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public ReactiveRedisTemplate<String, Object> objectReactiveRedisTemplate(
            ReactiveRedisConnectionFactory factory) {
        Jackson2JsonRedisSerializer<Object> serializer =
                new Jackson2JsonRedisSerializer<>(Object.class);
        RedisSerializationContext<String, Object> context =
                RedisSerializationContext.<String, Object>newSerializationContext(
                                new StringRedisSerializer())
                        .value(serializer)
                        .build();
        return new ReactiveRedisTemplate<>(factory, context);
    }
}