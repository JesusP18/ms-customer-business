package com.customer.business.service.impl;

import com.customer.business.event.dto.CustomerEvent;
import com.customer.business.exception.ResourceNotFoundException;
import com.customer.business.exception.ValidationException;
import com.customer.business.model.dto.ProductDTO;
import com.customer.business.resilience.ResilienceOperatorService;
import com.customer.business.validator.AddProductValidatorService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.repository.CustomerRepository;
import com.customer.business.service.CustomerService;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Servicio que encapsula la lógica de negocio relacionada con los clientes.
 * Se comunica con el repositorio {@link CustomerRepository} para persistencia en MongoDB.
 */
@Slf4j
@AllArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final WebClient productWebClient;

    private final AddProductValidatorService productValidatorService;

    private final CircuitBreaker productServiceCircuitBreaker;

    private final ResilienceOperatorService resilienceOperatorService;

    private final ReactiveRedisTemplate<String, Customer> redisTemplate;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    /**
     * Obtiene la lista de todos los clientes en la base de datos.
     *
     * @return lista de clientes
     */
    @Override
    public Flux<Customer> findAll() {
        return customerRepository.findAll();
    }

    /**
     * Busca un cliente por su identificador.
     * @param customerId identificador del cliente
     * @return Optional con el cliente si existe, vacío si no
     */
    @Override
    public Mono<Customer> findById(String customerId) {
        return redisTemplate.opsForValue().get(customerId)
                .switchIfEmpty(
                        customerRepository.findById(customerId)
                                .flatMap(customer ->
                                        redisTemplate.opsForValue()
                                                .set(customerId, customer)
                                                .thenReturn(customer)
                                )
                )
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer", customerId)));
    }

    /**
     * Crea un nuevo cliente en la base de datos.
     *
     * @param customer entidad del cliente a crear
     * @return cliente persistido
     */
    @Override
    public Mono<Customer> create(Customer customer) {
        return customerRepository.existsByDni(customer.getDni())
                .flatMap(exists -> {
                    if (exists) {
                        return Mono.error(new ValidationException("DNI already exists"));
                    }
                    return customerRepository.save(customer)
                            .flatMap(savedCustomer -> {
                                CustomerEvent event = new CustomerEvent(
                                        "CREATED",
                                        savedCustomer,
                                        LocalDateTime.now()
                                );
                                kafkaTemplate.send("customer-events",
                                        customer.getId(),
                                        event
                                );
                                return redisTemplate
                                        .opsForValue()
                                        .set(savedCustomer.getId(), savedCustomer)
                                        .thenReturn(savedCustomer);
                            });
                });
    }

    /**
     * * Actualiza un cliente existente.
     * - Si el cliente no existe, lanza una excepción.
     * - El ID del cliente se fuerza para coincidir con el recibido en el parámetro.
     *
     * @param customerId identificador del cliente
     * @param customer datos a actualizar
     * @return cliente actualizado
     * @throws IllegalArgumentException si el cliente no existe
     */
    // Método update usando Redis y Kafka
    @Override
    public Mono<Customer> update(String customerId, Customer customer) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer", customerId)))
                .flatMap(existing -> {
                    existing.setFirstName(customer.getFirstName());
                    existing.setLastName(customer.getLastName());
                    existing.setBusinessName(customer.getBusinessName());
                    existing.setAddress(customer.getAddress());
                    existing.setPhone(customer.getPhone());
                    existing.setEmail(customer.getEmail());
                    return customerRepository.save(existing)
                            .flatMap(updatedCustomer -> {
                                return sendCustomerEvent(
                                        "UPDATED",
                                        updatedCustomer
                                )
                                        .then(
                                                redisTemplate
                                                        .opsForValue()
                                                        .set(updatedCustomer.getId(),
                                                                updatedCustomer)
                                        )
                                        .thenReturn(updatedCustomer);
                            });
                });
    }

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param customerId identificador del cliente a eliminar
     */
    @Override
    public Mono<Void> delete(String customerId) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer", customerId)))
                .flatMap(customer -> {
                    return sendCustomerEvent("DELETED", customer)
                            .then(customerRepository.deleteById(customerId))
                            .then(redisTemplate.opsForValue().delete(customerId))
                            .then();
                });
    }

    /**
     * Agrega un producto a la lista de productos de un cliente.
     *
     * - Si el cliente no existe, lanza excepción.
     * - Si la lista de productos está vacía, se inicializa.
     * - Evita duplicados: solo agrega el producto si no está ya presente.
     *
     * @param customerId identificador del cliente
     * @param newProduct identificador del producto a agregar
     * @return cliente con la lista de productos actualizada
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Override
    public Mono<Void> addProduct(String customerId, Product newProduct) {
        if (newProduct == null) {
            return Mono.error(new ValidationException("Product data missing"));
        }

        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer", customerId)))
                .flatMap(customer ->
                        resilienceOperatorService.withCircuitBreaker(
                                        productWebClient.get()
                                                .uri("/{customerId}", customerId)
                                                .retrieve()
                                                .bodyToFlux(ProductDTO.class),
                                        productServiceCircuitBreaker
                                )
                                .collectList()
                                .flatMap(existingProducts -> {
                                    String customerType = productValidatorService
                                            .getCustomerType(customer.getCustomerType());
                                    String customerProfile = productValidatorService
                                            .getCustomerProfile(customer.getProfile());
                                    String productType = newProduct.getType();
                                    String productSubType = newProduct.getSubType();

                                    productValidatorService.validateBusinessRules(
                                            customerType, customerProfile, productType,
                                            productSubType, existingProducts);

                                    return createAndSendProductRequest(
                                            customerId, newProduct
                                    );
                                })
                                .onErrorMap(throwable ->
                                        new IllegalArgumentException(
                                                "Product service " +
                                                        "unavailable or " +
                                                        "timed out while " +
                                                        "fetching existing " +
                                                        "products",
                                                throwable
                                        )
                                )
                );
    }

    /**
     * Crea y envía la solicitud de producto al servicio externo
     */
    private Mono<Void> createAndSendProductRequest(String customerId, Product newProduct) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("customerId", customerId);
        requestBody.put("category", newProduct.getCategory());
        requestBody.put("type", newProduct.getType());
        requestBody.put("subType", newProduct.getSubType());

        Mono<Void> call = productWebClient.post()
                .uri("")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(Void.class);

        return resilienceOperatorService.withCircuitBreaker(call, productServiceCircuitBreaker)
                .onErrorMap(
                        throwable -> new IllegalArgumentException(
                                "Product service unavailable or timed out",
                                throwable));
    }

    /**
     * Elimina un producto de un cliente mediante comunicación
     * con el servicio externo de productos.
     *
     * - Si el cliente no existe, lanza excepción.
     * - Realiza una solicitud DELETE al servicio externo de productos.
     *
     * @param customerId identificador del cliente
     * @param productId identificador del producto a eliminar
     * @return Mono<Void> que indica la finalización de la operación
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Override
    public Mono<Void> removeProduct(String customerId, String productId) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(
                        new ResourceNotFoundException("Customer",
                                customerId))
                )
                .flatMap(customer -> {
                    Mono<Void> call = productWebClient.delete()
                            .uri(
                                    "/{productId}/customers/{customerId}",
                                    productId, customerId
                            )
                            .retrieve()
                            .bodyToMono(Void.class);
                    return resilienceOperatorService.withCircuitBreaker(
                            call, productServiceCircuitBreaker
                            )
                            .onErrorMap(
                                    throwable -> new IllegalArgumentException(
                                            "Product service unavailable or timed out",
                                            throwable)
                            );
                });
    }

    /**
     * Obtiene la lista de productos asociados a un cliente desde
     * el servicio externo de productos.
     *
     * - Si el cliente no existe, devuelve un error.
     * - Realiza una solicitud GET al servicio externo de productos.
     *
     * @param customerId identificador del cliente
     * @return Flux<Product> con los productos del cliente
     */
    @Override
    public Flux<Product> getProducts(String customerId) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("Customer", customerId)))
                .flatMapMany(customer ->
                        resilienceOperatorService.withCircuitBreaker(
                                productWebClient.get()
                                        .uri("/{customerId}", customerId)
                                        .retrieve()
                                        .bodyToFlux(Product.class),
                                productServiceCircuitBreaker
                        )
                );
    }

    private Mono<Void> sendCustomerEvent(String eventType, Customer customer) {
        return Mono.fromRunnable(() -> {
            CustomerEvent event = new CustomerEvent(eventType, customer, LocalDateTime.now());
            kafkaTemplate.send("customer-events", customer.getId(), event).addCallback(
                    result -> log.debug("Customer event sent successfully: {}", event),
                    ex -> log.error("Failed to send customer event: {}", event, ex)
            );
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
