package com.customer.business;

import com.customer.business.api.ApiApi;
import com.customer.business.mapper.CustomerMapper;
import com.customer.business.model.*;
import com.customer.business.model.entity.Product;
import com.customer.business.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Implementación de {@link ApiApi} para la API de clientes en modo reactivo (WebFlux).
 */
@Slf4j
@AllArgsConstructor
@Component
@RestController
public class CustomerApiImpl implements ApiApi {

    private final CustomerService customerService;
    private final CustomerMapper customerMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(Mono<CustomerRequest> customerRequest,
                                                                 ServerWebExchange exchange) {
        log.info("[CREATE_CUSTOMER] request received");
        return customerRequest
                .map(customerMapper::getCustomerofCustomerRequest)
                .flatMap(customerService::create) // Mono<Customer>
                .map(customerMapper::getCustomerResponseOfCustomer)
                .map(resp -> {
                    log.info("[CREATE_CUSTOMER] created id={}", resp.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
                })
                .doOnError(e -> log.error("[CREATE_CUSTOMER] error creating customer", e));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String customerId, ServerWebExchange exchange) {
        log.info("[DELETE_CUSTOMER] request id={}", customerId);
        return customerService.findById(customerId)
                .flatMap(c ->
                        customerService.delete(customerId)
                                .then(Mono.fromSupplier(() -> {
                                    log.info("[DELETE_CUSTOMER] deleted id={}", customerId);
                                    return ResponseEntity.noContent().<Void>build();
                                }))
                )
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.warn("[DELETE_CUSTOMER] not found id={}", customerId);
                    return ResponseEntity.notFound().build();
                }))
                .doOnError(e -> log.error("[DELETE_CUSTOMER] error id={}", customerId, e));
    }

    @Override
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllCustomers(ServerWebExchange exchange) {
        log.info("[GET_ALL_CUSTOMERS] request");
        Flux<CustomerResponse> body = customerService.findAll()
                .map(customerMapper::getCustomerResponseOfCustomer);
        return Mono.just(ResponseEntity.ok(body));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(String customerId, ServerWebExchange exchange) {
        log.info("[GET_CUSTOMER_BY_ID] request id={}", customerId);
        return customerService.findById(customerId)
                .map(customerMapper::getCustomerResponseOfCustomer)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.warn("[GET_CUSTOMER_BY_ID] not found id={}", customerId);
                    return ResponseEntity.notFound().build();
                }))
                .doOnError(e -> log.error("[GET_CUSTOMER_BY_ID] error id={}", customerId, e));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(String customerId,
                                                                 Mono<CustomerRequest> customerRequest,
                                                                 ServerWebExchange exchange) {
        log.info("[UPDATE_CUSTOMER] request id={}", customerId);
        return customerRequest
                .map(customerMapper::getCustomerofCustomerRequest)
                .flatMap(request -> customerService.update(customerId, request))
                .map(customerMapper::getCustomerResponseOfCustomer)
                .map(ResponseEntity::ok)
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("[UPDATE_CUSTOMER] not found id={}", customerId);
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .doOnError(error -> log.error("[UPDATE_CUSTOMER] error id={}", customerId, error));
    }

    @Override
    public Mono<ResponseEntity<Flux<ProductResponse>>> getCustomerProducts(String id, ServerWebExchange exchange) {
        log.info("[GET_CUSTOMER_PRODUCTS] request id={}", id);

        Flux<ProductResponse> productResponses = customerService.getProductIds(id)
                .map(product -> {
                    ProductResponse response = new ProductResponse();
                    response.setId(product.getId());
                    return response;
                });

        return Mono.just(ResponseEntity.ok().body(productResponses));
    }

    @Override
    public Mono<ResponseEntity<Void>> addProductToCustomer(String id,
                                                           Mono<ProductRequest> productRequest,
                                                           ServerWebExchange exchange) {
        log.info("[ADD_PRODUCT_TO_CUSTOMER] request id={}", id);

        return productRequest
                .flatMap(request -> {
                    if (request == null || request.getId() == null || request.getId().isBlank()) {
                        log.warn("[ADD_PRODUCT_TO_CUSTOMER] invalid request for id={}", id);
                        return Mono.just(ResponseEntity.badRequest().<Void>build());
                    }
                    return customerService.addProduct(id, request.getId())
                            .then(Mono.fromSupplier(() -> {
                                log.info("[ADD_PRODUCT_TO_CUSTOMER] added product={} to id={}", request.getId(), id);
                                return ResponseEntity.noContent().<Void>build();
                            }))
                            .onErrorResume(IllegalArgumentException.class, ex -> {
                                log.warn("[ADD_PRODUCT_TO_CUSTOMER] customer not found id={}", id);
                                return Mono.just(ResponseEntity.notFound().build());
                            });
                })
                .switchIfEmpty(Mono.just(ResponseEntity.badRequest().build()))
                .doOnError(error -> log.error("[ADD_PRODUCT_TO_CUSTOMER] error id={}", id, error));
    }

    @Override
    public Mono<ResponseEntity<Void>> removeProductFromCustomer(String customerId,
                                                                String productId,
                                                                ServerWebExchange exchange) {
        log.info("[REMOVE_PRODUCT_FROM_CUSTOMER] request id={}, productId={}", customerId, productId);
        return customerService.removeProduct(customerId, productId)
                .then(Mono.fromSupplier(() -> {
                    log.info("[REMOVE_PRODUCT_FROM_CUSTOMER] removed product={} from id={}", productId, customerId);
                    return ResponseEntity.noContent().<Void>build();
                }))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("[REMOVE_PRODUCT_FROM_CUSTOMER] not found id={}", customerId);
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .doOnError(e -> log.error("[REMOVE_PRODUCT_FROM_CUSTOMER] error id={}, productId={}", customerId, productId, e));
    }
}
