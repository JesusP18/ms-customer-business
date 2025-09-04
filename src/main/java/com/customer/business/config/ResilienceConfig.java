package com.customer.business.config;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceConfig {

    private static final String PRODUCT_SERVICE_CB = "productService";

    @Bean
    public CircuitBreaker productServiceCircuitBreaker(
            CircuitBreakerRegistry registry) {
        return registry.circuitBreaker(PRODUCT_SERVICE_CB);
    }
}