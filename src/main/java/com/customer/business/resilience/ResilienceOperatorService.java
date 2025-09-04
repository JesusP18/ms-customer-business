package com.customer.business.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public class ResilienceOperatorService {
    private static final Logger log = LoggerFactory.getLogger(ResilienceOperatorService.class);

    private static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(2);

    /**
     * Aplica CircuitBreaker + timeout a un Mono<T>.
     *
     * @param mono Mono a proteger
     * @param circuitBreaker CircuitBreaker (por nombre o bean)
     * @param <T> tipo
     * @return Mono protegido
     */
    public <T> Mono<T> withCircuitBreaker(Mono<T> mono, CircuitBreaker circuitBreaker) {
        if (mono == null) {
            return Mono.empty();
        }
        return mono
                .timeout(DEFAULT_TIMEOUT)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorMap(throwable -> {
                    log.warn("[Resilience] downstream failure (cb={}): {}",
                            circuitBreaker.getName(), throwable.toString());
                    return new RuntimeException(
                            "Downstream service unavailable or timed out",
                            throwable
                    );
                });
    }

    /**
     * Aplica CircuitBreaker + timeout a un Flux<T>.
     */
    public <T> Flux<T> withCircuitBreaker(Flux<T> flux, CircuitBreaker circuitBreaker) {
        if (flux == null) {
            return Flux.empty();
        }
        return flux
                .timeout(DEFAULT_TIMEOUT)
                .transform(CircuitBreakerOperator.of(circuitBreaker))
                .onErrorResume(throwable -> {
                    log.warn("[Resilience] flux fallback (cb={}): {}",
                            circuitBreaker.getName(),
                            throwable.toString()
                    );
                    return Flux.empty();
                });
    }
}