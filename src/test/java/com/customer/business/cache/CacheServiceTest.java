package com.customer.business.cache;

import com.customer.business.model.entity.Customer;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

class CacheServiceTest {

    @Mock
    private ReactiveRedisTemplate<String, Object> redisTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ReactiveValueOperations<String, Object> valueOperations;

    @InjectMocks
    private CacheService cacheService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("cacheCustomer debe guardar correctamente")
    void cacheCustomerShouldStoreCustomer() {
        Customer customer = new Customer();
        when(valueOperations.set(eq("key"), eq(customer), any(Duration.class)))
            .thenReturn(Mono.just(true));
        StepVerifier.create(cacheService.cacheCustomer("key", customer))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("getCachedCustomer debe devolver Customer si ya es instancia")
    void getCachedCustomerShouldReturnCustomerInstance() {
        Customer customer = new Customer();
        when(valueOperations.get("key")).thenReturn(Mono.just(customer));
        StepVerifier.create(cacheService.getCachedCustomer("key"))
                .expectNext(customer)
                .verifyComplete();
    }

    @Test
    @DisplayName("getCachedCustomer debe convertir si no es instancia")
    void getCachedCustomerShouldConvertIfNotInstance() {
        Object obj = new Object();
        Customer customer = new Customer();
        when(valueOperations.get("key")).thenReturn(Mono.just(obj));
        when(objectMapper.convertValue(obj, Customer.class)).thenReturn(customer);
        StepVerifier.create(cacheService.getCachedCustomer("key"))
                .expectNext(customer)
                .verifyComplete();
    }

    @Test
    @DisplayName("evictCustomer debe eliminar correctamente")
    void evictCustomerShouldDelete() {
        when(valueOperations.delete("key")).thenReturn(Mono.just(true));
        StepVerifier.create(cacheService.evictCustomer("key"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("cacheObject debe guardar correctamente")
    void cacheObjectShouldStore() {
        when(valueOperations.set(eq("key"), eq("value"), any(Duration.class)))
            .thenReturn(Mono.just(true));
        StepVerifier.create(cacheService.cacheObject("key", "value", Duration.ofMinutes(5)))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("getCachedObject debe devolver el objeto")
    void getCachedObjectShouldReturnObject() {
        when(valueOperations.get("key")).thenReturn(Mono.just("value"));
        StepVerifier.create(cacheService.getCachedObject("key"))
                .expectNext("value")
                .verifyComplete();
    }

    @Test
    @DisplayName("cacheCustomer maneja error de Redis")
    void cacheCustomerHandlesRedisError() {
        Customer customer = new Customer();
        when(valueOperations.set(eq("key"), eq(customer), any(Duration.class)))
            .thenReturn(Mono.error(new RuntimeException("Redis error")));
        StepVerifier.create(cacheService.cacheCustomer("key", customer))
            .expectErrorMatches(e -> e.getMessage().equals("Redis error"))
            .verify();
    }

    @Test
    @DisplayName("evictCustomer maneja error de Redis")
    void evictCustomerHandlesRedisError() {
        when(valueOperations.delete("key"))
            .thenReturn(Mono.error(new RuntimeException("Redis error")));
        StepVerifier.create(cacheService.evictCustomer("key"))
            .expectErrorMatches(e -> e.getMessage().equals("Redis error"))
            .verify();
    }

    @Test
    @DisplayName("cacheObject maneja error de Redis")
    void cacheObjectHandlesRedisError() {
        when(valueOperations.set(eq("key"), eq("value"), any(Duration.class)))
            .thenReturn(Mono.error(new RuntimeException("Redis error")));
        StepVerifier.create(
            cacheService.cacheObject("key", "value", Duration.ofMinutes(5))
        )
            .expectErrorMatches(e -> e.getMessage().equals("Redis error"))
            .verify();
    }

    @Test
    @DisplayName("getCachedObject maneja error de Redis")
    void getCachedObjectHandlesRedisError() {
        when(valueOperations.get("key"))
            .thenReturn(Mono.error(new RuntimeException("Redis error")));
        StepVerifier.create(cacheService.getCachedObject("key"))
            .expectErrorMatches(e -> e.getMessage().equals("Redis error"))
            .verify();
    }
}
