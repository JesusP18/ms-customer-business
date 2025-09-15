package com.customer.business.cache;

import com.customer.business.model.entity.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;

@AllArgsConstructor
@Service
public class CacheService {

    private final ReactiveRedisTemplate<String, Object> redisTemplate;

    private final ObjectMapper objectMapper;

    public Mono<Boolean> cacheCustomer(String key, Customer customer) {
        return redisTemplate.opsForValue().set(key, customer, Duration.ofHours(1));
    }

//    public Mono<Customer> getCachedCustomer(String key) {
//        return redisTemplate.opsForValue().get(key)
//                .cast(Customer.class);
//    }

    public Mono<Customer> getCachedCustomer(String key) {
        return redisTemplate.opsForValue().get(key)
                .flatMap(obj -> {
                    if (obj instanceof Customer) {
                        return Mono.just((Customer) obj);
                    } else {
                        return Mono.just(objectMapper.convertValue(obj, Customer.class));
                    }
                });
    }

    public Mono<Boolean> evictCustomer(String key) {
        return redisTemplate.opsForValue().delete(key);
    }

    public Mono<Boolean> cacheObject(String key, Object value, Duration duration) {
        return redisTemplate.opsForValue().set(key, value, duration);
    }

    public Mono<Object> getCachedObject(String key) {
        return redisTemplate.opsForValue().get(key);
    }
}