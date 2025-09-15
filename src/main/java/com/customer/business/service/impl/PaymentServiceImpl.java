package com.customer.business.service.impl;

import com.customer.business.model.PaymentRequest;
import com.customer.business.model.PaymentResponse;
import com.customer.business.resilience.ResilienceOperatorService;
import com.customer.business.service.PaymentService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementación de {@link PaymentService} que se comunica con
 * el servicio externo de productos mediante {@link WebClient}.
 */
@Slf4j
@Service
public class PaymentServiceImpl implements PaymentService {

    private final WebClient productWebClient;

    private final CircuitBreaker productServiceCircuitBreaker;

    private final ResilienceOperatorService resilience;

    public PaymentServiceImpl(WebClient productWebClient,
                              CircuitBreakerRegistry cbRegistry,
                              TimeLimiterRegistry tlRegistry,
                              ResilienceOperatorService resilience) {
        this.productWebClient = productWebClient;
        this.productServiceCircuitBreaker = cbRegistry.circuitBreaker("productService");
        this.resilience = resilience;
    }

    @Override
    public Mono<PaymentResponse> payCreditProduct(String customerId, PaymentRequest request) {
        return productWebClient.post()
                .uri("/products/{productId}/pay", request.getTargetProductId())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PaymentResponse.class)
                .transform(
                        call -> resilience.withCircuitBreaker(
                                call, productServiceCircuitBreaker
                        )
                )
                .doOnSuccess(response -> {
                    log.debug("Payment processed successfully for customer: {}, product: {}",
                            customerId, request.getTargetProductId());
                })
                .onErrorResume(ex -> {
                    log.error("Payment processing failed: {}", ex.getMessage());
                    return Mono.just(
                            new PaymentResponse().status("FAILED").message(ex.getMessage())
                    );
                });
    }
}
