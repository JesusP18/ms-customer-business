package com.customer.business;

import com.customer.business.api.ApiApi;
import com.customer.business.exception.ResourceNotFoundException;
import com.customer.business.exception.ValidationException;
import com.customer.business.model.CustomerCreateRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.CustomerUpdateRequest;
import com.customer.business.model.DebitCardAssociationRequest;
import com.customer.business.model.DebitCardBalanceResponse;
import com.customer.business.model.PaymentRequest;
import com.customer.business.model.PaymentResponse;
import com.customer.business.model.ProductReportResponse;
import com.customer.business.model.ProductRequest;
import com.customer.business.model.ProductResponse;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.service.DebitCardService;
import com.customer.business.service.PaymentService;
import com.customer.business.service.ReportService;
import com.customer.business.validator.CreateCustomerValidator;
import com.customer.business.validator.UpdateCustomerValidator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import com.customer.business.mapper.CustomerMapper;
import com.customer.business.service.CustomerService;

import java.time.LocalDate;

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

    private final UpdateCustomerValidator updateValidator;

    private final CreateCustomerValidator createValidator;

    private final PaymentService paymentService;

    private final DebitCardService debitCardService;

    private final ReportService reportService;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            Mono<CustomerCreateRequest> customerRequest, ServerWebExchange exchange) {
        log.info("[CREATE_CUSTOMER] request received");
        return customerRequest
                .doOnNext(createValidator::validate) // Validar la solicitud
                .map(customerMapper::getCustomerofCustomerCreateRequest)
                .flatMap(customerService::create)
                .map(customerMapper::getCustomerResponseOfCustomer)
                .map(resp -> {
                    log.info("[CREATE_CUSTOMER] created id={}", resp.getId());
                    return ResponseEntity.status(HttpStatus.CREATED).body(resp);
                })
                .onErrorResume(ValidationException.class, ex -> {
                    log.warn("[CREATE_CUSTOMER] validation failed: {}", ex.getMessage());
                    return Mono.error(new ValidationException(ex.getMessage()));
                })
                .doOnError(e -> log.error("[CREATE_CUSTOMER] error creating customer", e));
    }

    @Override
    public Mono<ResponseEntity<Void>> deleteCustomer(String customerId,
                                                     ServerWebExchange exchange) {
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
    public Mono<ResponseEntity<Flux<CustomerResponse>>> getAllCustomers(
            ServerWebExchange exchange) {
        log.info("[GET_ALL_CUSTOMERS] request");
        Flux<CustomerResponse> body = customerService.findAll()
                .map(customerMapper::getCustomerResponseOfCustomer);
        return Mono.just(ResponseEntity.ok(body));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(String customerId,
                                                                  ServerWebExchange exchange) {
        log.info("[GET_CUSTOMER_BY_ID] request id={}", customerId);
        return customerService.findById(customerId)
                .map(customerMapper::getCustomerResponseOfCustomer)
                .map(ResponseEntity::ok)
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.warn("[GET_CUSTOMER_BY_ID] not found id={}", customerId);
                    return ResponseEntity.notFound().build();
                }))
                .doOnError(error -> log.error("[GET_CUSTOMER_BY_ID] error id={}",
                        customerId, error));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> updateCustomer(
            String customerId,
            Mono<CustomerUpdateRequest> customerRequest,
            ServerWebExchange exchange) {
        log.info("[UPDATE_CUSTOMER] request id={}", customerId);
        return customerRequest
                .doOnNext(updateValidator::validate) // Validar la solicitud
                .flatMap(request -> customerService.findById(customerId)
                        .flatMap(existing -> {
                            Customer updated = customerMapper
                                    .getCustomerFromUpdateRequest(request, existing);
                            return customerService.update(customerId, updated);
                        }))
                .map(customerMapper::getCustomerResponseOfCustomer)
                .map(ResponseEntity::ok)
                .onErrorResume(ValidationException.class, ex -> {
                    log.warn("[UPDATE_CUSTOMER] validation failed id={}: {}",
                            customerId, ex.getMessage());
                    return Mono.error(new ValidationException(ex.getMessage()));
                })
                .onErrorResume(ResourceNotFoundException.class, ex -> {
                    log.warn("[UPDATE_CUSTOMER] not found id={}", customerId);
                    return Mono.error(new ResourceNotFoundException("Customer", customerId));
                })
                .doOnError(error -> log.error("[UPDATE_CUSTOMER] error id={}",
                        customerId, error));
    }

    @Override
    public Mono<ResponseEntity<Flux<ProductResponse>>> getCustomerProducts(
            String id, ServerWebExchange exchange) {
        log.info("[GET_CUSTOMER_PRODUCTS] request id={}", id);

        Flux<ProductResponse> productResponses = customerService.getProducts(id)
                .map(product -> {
                    ProductResponse response = new ProductResponse();
                    response.setCategory(
                            ProductResponse.CategoryEnum.fromValue(
                                    product.getCategory()));
                    response.setType(
                            ProductResponse.TypeEnum.fromValue(
                                    product.getType()));
                    response.setSubType(
                            ProductResponse.SubTypeEnum.fromValue(
                                    product.getSubType()));
                    return response;
                });

        return Mono.just(ResponseEntity.ok().body(productResponses));
    }

    @Override
    public Mono<ResponseEntity<Void>> addProductToCustomer(String id,
                                                           Mono<ProductRequest> productRequestMono,
                                                           ServerWebExchange exchange) {
        log.info("[ADD_PRODUCT_TO_CUSTOMER] request received for customer id={}", id);
        return productRequestMono
                .flatMap(productRequest -> {

                    // Mapear ProductRequest -> entidad Product (usa el constructor de 4 args)
                    Product productEntity = new Product(
                            productRequest.getCustomerId(),
                            productRequest.getCategory().getValue(),
                            productRequest.getType().getValue(),
                            productRequest.getSubType().getValue()
                    );

                    // Llamar al service reactivo sin bloquear
                    return customerService.addProduct(id, productEntity)
                            .then(Mono.fromSupplier(() -> {
                                log.info(
                                        "[ADD_PRODUCT_TO_CUSTOMER] product added " +
                                                "successfully for customer id={}, product id={}",
                                        id, productRequest);
                                return ResponseEntity.noContent().<Void>build();
                            }))
                            .onErrorResume(ex -> {
                                // Map business rule violations to appropriate HTTP status codes
                                if (ex instanceof IllegalArgumentException) {
                                    String msg = ex.getMessage() == null ?
                                            "" : ex.getMessage().toLowerCase();
                                    log.warn(
                                            "[ADD_PRODUCT_TO_CUSTOMER] " +
                                                    "business rule " +
                                                    "violation for customer id={}: {}",
                                            id, ex.getMessage());

                                    if (msg.contains("not found")) {
                                        return Mono.just(ResponseEntity.notFound().build());
                                    }
                                    return Mono.just(ResponseEntity.status(
                                            HttpStatus.BAD_REQUEST).build());
                                }
                                log.error(
                                        "[ADD_PRODUCT_TO_CUSTOMER]" +
                                                " unexpected error for customer id={}", id, ex);
                                return Mono.error(ex);
                            });
                })
                .switchIfEmpty(Mono.fromSupplier(() -> {
                    log.warn("[ADD_PRODUCT_TO_CUSTOMER]" +
                            " empty request body for customer id={}", id);
                    return ResponseEntity.badRequest().<Void>build();
                }))
                .doOnError(error ->
                        log.error("[ADD_PRODUCT_TO_CUSTOMER]" +
                                " processing error for customer id={}", id, error));
    }

    @Override
    public Mono<ResponseEntity<Void>> removeProductFromCustomer(String customerId,
                                                                String productId,
                                                                ServerWebExchange exchange) {
        log.info("[REMOVE_PRODUCT_FROM_CUSTOMER] request id={}, productId={}",
                customerId, productId);
        return customerService.removeProduct(customerId, productId)
                .then(Mono.fromSupplier(() -> {
                    log.info("[REMOVE_PRODUCT_FROM_CUSTOMER] removed product={} from id={}",
                            productId, customerId);
                    return ResponseEntity.noContent().<Void>build();
                }))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("[REMOVE_PRODUCT_FROM_CUSTOMER] not found id={}", customerId);
                    return Mono.just(ResponseEntity.notFound().build());
                })
                .doOnError(error ->
                        log.error("[REMOVE_PRODUCT_FROM_CUSTOMER] error id={}, productId={}",
                                customerId, productId, error));
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> payCreditProduct(
            String id,
            Mono<PaymentRequest> req,
            ServerWebExchange exchange) {
        log.info("[PAY_CREDIT_PRODUCT] request customerId={}", id);
        return req.flatMap(r -> paymentService.payCreditProduct(id, r))
                .map(customerMapper::mapToPaymentResponse)
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info(
                        "[PAY_CREDIT_PRODUCT] payment processed for customerId={}", id))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn(
                            "[PAY_CREDIT_PRODUCT] business error for customerId={}: {}",
                            id, ex.getMessage()
                    );
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(ResourceNotFoundException.class, ex -> {
                    log.warn("[PAY_CREDIT_PRODUCT] not found customerId={}", id);
                    return Mono.error(new ResourceNotFoundException("Customer", id));
                })
                .doOnError(
                        error -> log.error(
                                "[PAY_CREDIT_PRODUCT] unexpected error for customerId={}",
                                id, error)
                );
    }

    @Override
    public Mono<ResponseEntity<String>> associateDebitCard(
            String id, Mono<DebitCardAssociationRequest> req,
            ServerWebExchange exchange) {
        log.info("[ASSOCIATE_DEBIT_CARD] request customerId={}", id);

        return req.flatMap(r -> debitCardService.associateDebitCard(id, r))
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info(
                        "[ASSOCIATE_DEBIT_CARD] debit card associated for customerId={}", id))
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn(
                            "[ASSOCIATE_DEBIT_CARD] business rule violation for customerId={}: {}",
                            id, ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .onErrorResume(ResourceNotFoundException.class, ex -> {
                    log.warn("[ASSOCIATE_DEBIT_CARD] not found customerId={}", id);
                    return Mono.error(new ResourceNotFoundException("Customer", id));
                })
                .doOnError(error -> log.error(
                        "[ASSOCIATE_DEBIT_CARD] unexpected error for customerId={}", id, error));
    }

    @Override
    public Mono<ResponseEntity<DebitCardBalanceResponse>> getMainAccountBalance(
            String customerId, String cardId, ServerWebExchange exchange) {
        log.info("[GET_MAIN_ACCOUNT_BALANCE] request customerId={}, cardId={}", customerId, cardId);

        return debitCardService.getMainAccountId(customerId, cardId) // primero resolver productId
                .flatMap(productId -> debitCardService.getMainAccountBalance(productId, cardId))
                .map(ResponseEntity::ok)
                .doOnSuccess(resp -> log.info(
                        "[GET_MAIN_ACCOUNT_BALANCE] balance retrieved for customerId={}, cardId={}",
                        customerId, cardId))
                .onErrorResume(ResourceNotFoundException.class, ex -> {
                    log.warn("[GET_MAIN_ACCOUNT_BALANCE] not found cardId={} for customerId={}",
                            cardId, customerId);
                    return Mono.error(new ResourceNotFoundException("Customer", customerId));
                })
                .onErrorResume(IllegalArgumentException.class, ex -> {
                    log.warn("[GET_MAIN_ACCOUNT_BALANCE] " +
                                    "invalid request for customerId={}, cardId={}: {}",
                            customerId, cardId, ex.getMessage());
                    return Mono.just(ResponseEntity.badRequest().build());
                })
                .doOnError(error -> log.error(
                        "[GET_MAIN_ACCOUNT_BALANCE] " +
                                "error retrieving balance for customerId={}, cardId={}",
                        customerId, cardId, error));
    }


    @Override
    public Mono<ResponseEntity<Flux<ProductReportResponse>>> getProductReports(
            LocalDate from,
            LocalDate to,
            ServerWebExchange exchange) {
        log.info("[GET_PRODUCT_REPORTS] request from={} to={}", from, to);

        Flux<ProductReportResponse> mapped = reportService.generateProductReport(from, to)
                .map(customerMapper::mapToProductReportResponse);

        return Mono.just(ResponseEntity.ok(mapped))
                .doOnSuccess(
                        resp -> log.info("[GET_PRODUCT_REPORTS]" +
                                " report generated from={} to={}", from, to))
                .onErrorResume(
                        IllegalArgumentException.class,
                        ex -> {
                            log.warn(
                                    "[GET_PRODUCT_REPORTS] " +
                                            "invalid parameters from={} to={}: {}",
                                    from,
                                    to,
                                    ex.getMessage()
                            );
                            return Mono.just(ResponseEntity.badRequest().build());
                        })
                .doOnError(
                        error ->
                                log.error("[GET_PRODUCT_REPORTS] " +
                                "error generating report from={} to={}",
                                        from, to, error
                                )
                );
    }
}
