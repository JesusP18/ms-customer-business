package com.customer.business.service;

import com.customer.business.model.DebitCardAssociationRequest;
import com.customer.business.model.DebitCardBalanceResponse;
import reactor.core.publisher.Mono;

/**
 * Servicio para operaciones de tarjetas de débito:
 * - Asociación de tarjetas a cuentas
 * - Consulta de balance de la cuenta principal vinculada a una tarjeta
 */
public interface DebitCardService {

    /**
     * Asocia una tarjeta de débito a una o varias cuentas de un cliente.
     *
     * La asociación se delega al servicio externo de productos.
     *
     * @param customerId ID del cliente que realiza la asociación
     * @param request    payload que contiene el {@code cardId} y la lista de {@code accountIds}
     * @return Mono con un mensaje de confirmación en caso de éxito
     * @throws IllegalArgumentException si falla la comunicación con el servicio externo
     */
    Mono<String> associateDebitCard(String customerId, DebitCardAssociationRequest request);

    /**
     * Obtiene el balance de la cuenta principal vinculada a una tarjeta de débito.
     *
     * Se consulta al servicio externo: {@code /products/{productId}/debit-cards/{cardId}/balance}.
     *
     * @param productId ID del producto (cuenta principal) vinculado a la tarjeta
     * @param cardId    ID de la tarjeta de débito
     * @return Mono con los datos de balance (cardId, productId y balance)
     * @throws IllegalArgumentException si falla la comunicación con el servicio externo
     */
    Mono<DebitCardBalanceResponse> getMainAccountBalance(String productId, String cardId);

    Mono<String> getMainAccountId(String customerId, String cardId);
}
