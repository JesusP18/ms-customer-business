package com.customer.business.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

class ResilienceOperatorServiceTest {

    private ResilienceOperatorService resilience;

    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        // Usar un CircuitBreaker real para los tests de éxito
        circuitBreaker = CircuitBreaker.of("testCB", CircuitBreakerConfig.ofDefaults());
        resilience = new ResilienceOperatorService();
    }

    @Test
    @DisplayName("withCircuitBreaker Mono - éxito")
    void withCircuitBreakerMonoSuccess() {
        Mono<String> mono = Mono.just("ok");
        StepVerifier.create(resilience.withCircuitBreaker(mono, circuitBreaker))
                .expectNext("ok")
                .verifyComplete();
    }

    @Test
    @DisplayName("withCircuitBreaker Mono - error mapeado")
    void withCircuitBreakerMonoError() {
        // Usar un mock solo para el nombre en el test de error
        CircuitBreaker cbMock = org.mockito.Mockito.mock(CircuitBreaker.class);
        org.mockito.Mockito.when(cbMock.getName()).thenReturn("testCB");
        Mono<String> mono = Mono.error(new RuntimeException("fail"));
        StepVerifier.create(resilience.withCircuitBreaker(mono, cbMock))
                .expectErrorMatches(e -> e instanceof RuntimeException
                        && e.getMessage().contains("Downstream service unavailable"))
                .verify();
    }

    @Test
    @DisplayName("withCircuitBreaker Mono - null")
    void withCircuitBreakerMonoNull() {
        StepVerifier.create(resilience.withCircuitBreaker((Mono<String>) null, circuitBreaker))
                .verifyComplete();
    }

    @Test
    @DisplayName("withCircuitBreaker Flux - éxito")
    void withCircuitBreakerFluxSuccess() {
        Flux<String> flux = Flux.just("a", "b");
        StepVerifier.create(resilience.withCircuitBreaker(flux, circuitBreaker))
                .expectNext("a", "b")
                .verifyComplete();
    }

    @Test
    @DisplayName("withCircuitBreaker Flux - error fallback")
    void withCircuitBreakerFluxError() {
        CircuitBreaker cbMock = org.mockito.Mockito.mock(CircuitBreaker.class);
        org.mockito.Mockito.when(cbMock.getName()).thenReturn("testCB");
        Flux<String> flux = Flux.error(new RuntimeException("fail"));
        StepVerifier.create(resilience.withCircuitBreaker(flux, cbMock))
                .verifyComplete();
    }

    @Test
    @DisplayName("withCircuitBreaker Flux - null")
    void withCircuitBreakerFluxNull() {
        StepVerifier.create(resilience.withCircuitBreaker((Flux<String>) null, circuitBreaker))
                .verifyComplete();
    }
}
