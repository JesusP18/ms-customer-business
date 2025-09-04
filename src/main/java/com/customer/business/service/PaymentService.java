package com.customer.business.service;

import com.customer.business.model.PaymentRequest;
import com.customer.business.model.PaymentResponse;
import reactor.core.publisher.Mono;

/**
 * Servicio encargado de procesar pagos de productos de crédito.
 */
public interface PaymentService {
    /**
     * Realiza el pago de un producto de crédito de un cliente.
     * @param customerId identificador del cliente
     * @param request detalles del pago
     * @return respuesta con estado y mensaje
     */
    Mono<PaymentResponse> payCreditProduct(String customerId, PaymentRequest request);
}
