package com.customer.business.service.impl;

import com.customer.business.model.DebitCardAssociationRequest;
import com.customer.business.model.DebitCardBalanceResponse;
import com.customer.business.resilience.ResilienceOperatorService;
import com.customer.business.service.DebitCardService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

/**
 * Implementación en memoria de {@link DebitCardService}.
 * Maneja la asociación de tarjetas de débito con cuentas.
 */
@Service
public class DebitCardServiceImpl implements DebitCardService {

    private final WebClient productWebClient;

    private final CircuitBreaker productServiceCircuitBreaker;

    private final ResilienceOperatorService resilience;

    public DebitCardServiceImpl(WebClient productWebClient,
                            CircuitBreakerRegistry cbRegistry,
                            ResilienceOperatorService resilience) {
        this.productWebClient = productWebClient;
        this.productServiceCircuitBreaker = cbRegistry.circuitBreaker("productService");
        this.resilience = resilience;
    }

    /**
     * Asocia una tarjeta de débito a una o varias cuentas de un cliente.
     *
     * @param customerId identificador del cliente
     * @param request    objeto con cardId y lista de accountIds
     * @return mensaje de éxito
     */
    public Mono<String> associateDebitCard(String customerId, DebitCardAssociationRequest request) {
        return productWebClient.post()
                .uri("/customers/{customerId}/debit-cards/associate", customerId)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(String.class)
                .transform(
                        call ->
                                resilience.withCircuitBreaker(
                                        call, productServiceCircuitBreaker)
                )
                .onErrorMap(
                        ex -> new IllegalArgumentException(
                                "No se pudo asociar la tarjeta", ex)
                );
    }

    /**
     * Obtiene el balance de la cuenta principal vinculada a una tarjeta de débito.
     *
     * @param productId identificador del producto principal
     * @param cardId    identificador de la tarjeta de débito
     * @return objeto con cardId, productId y balance
     */
    public Mono<DebitCardBalanceResponse> getMainAccountBalance(String productId, String cardId) {
        Mono<DebitCardBalanceResponse> call = productWebClient.get()
                .uri("/products/{productId}/debit-cards/{cardId}/balance",
                        productId, cardId)
                .retrieve()
                .bodyToMono(DebitCardBalanceResponse.class);

        return resilience.withCircuitBreaker(call, productServiceCircuitBreaker)
                .onErrorMap(
                        ex -> new IllegalArgumentException(
                                "No se pudo obtener el balance", ex)
                );
    }

    @Override
    public Mono<String> getMainAccountId(String customerId, String cardId) {
        return productWebClient.get()
                .uri("/products/{customerId}/debit-cards/{cardId}/main-account", customerId, cardId)
                .retrieve()
                .bodyToMono(String.class)
                .transform(
                        call -> resilience.withCircuitBreaker(call, productServiceCircuitBreaker)
                )
                .onErrorMap(
                        ex -> new IllegalArgumentException(
                                "No se pudo obtener el productId principal", ex)
                );
    }
}