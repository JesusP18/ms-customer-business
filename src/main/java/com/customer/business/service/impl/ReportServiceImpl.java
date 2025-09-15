package com.customer.business.service.impl;

import com.customer.business.model.ProductReportResponse;
import com.customer.business.resilience.ResilienceOperatorService;
import com.customer.business.service.ReportService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

/**
 * Implementación de {@link ReportService} que consulta
 * el servicio externo de productos para generar reportes.
 */
@Slf4j
@Service
public class ReportServiceImpl implements ReportService {

    private final WebClient productWebClient;

    private final CircuitBreaker productServiceCircuitBreaker;

    private final ResilienceOperatorService resilience;

    public ReportServiceImpl(WebClient productWebClient,
                             CircuitBreakerRegistry cbRegistry,
                             TimeLimiterRegistry tlRegistry,
                             ResilienceOperatorService resilience) {
        this.productWebClient = productWebClient;
        this.productServiceCircuitBreaker = cbRegistry.circuitBreaker("productService");
        this.resilience = resilience;
    }

    @Override
    public Flux<ProductReportResponse> generateProductReport(LocalDate from, LocalDate to) {
        return productWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/products/report")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .build())
                .retrieve()
                .bodyToFlux(ProductReportResponse.class)
                .transform(
                        call -> resilience.withCircuitBreaker(
                                call, productServiceCircuitBreaker
                        )
                )
                .doOnNext(report ->
                        log.debug("Report generated for period: {} to {}", from, to))
                .onErrorResume(ex -> Flux.empty());
    }
}