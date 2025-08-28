package com.customer.business.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.repository.CustomerRepository;
import com.customer.business.service.CustomerService;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que encapsula la lógica de negocio relacionada con los clientes.
 * Se comunica con el repositorio {@link CustomerRepository} para persistencia en MongoDB.
 */
@AllArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

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
        return customerRepository.findById(customerId);
    }

    /**
     * Crea un nuevo cliente en la base de datos.
     *
     * @param customer entidad del cliente a crear
     * @return cliente persistido
     */
    @Override
    public Mono<Customer> create(Customer customer) {
        return customerRepository.save(customer);
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
    @Override
    public Mono<Customer> update(String customerId, Customer customer) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(existing -> {
                    existing.setCustomerType(customer.getCustomerType());
                    existing.setFirstName(customer.getFirstName());
                    existing.setLastName(customer.getLastName());
                    existing.setBusinessName( customer.getBusinessName());
                    existing.setDni(customer.getDni());
                    existing.setRuc(customer.getRuc());
                    existing.setAddress(customer.getAddress());
                    existing.setPhone(customer.getPhone());
                    existing.setEmail(customer.getEmail());
                    if (customer.getProducts() != null) {
                        existing.setProducts(customer.getProducts());
                    }
                    return customerRepository.save(existing);
                });
    }

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param customerId identificador del cliente a eliminar
     */
    @Override
    public Mono<Void> delete(String customerId) {
        return customerRepository.deleteById(customerId);
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
        if (newProduct == null || newProduct.getId() == null || newProduct.getId().isBlank()) {
            return Mono.error(new IllegalArgumentException("product data missing"));
        }
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(customer -> {
                    if (customer.getProducts() == null) {
                        customer.setProducts(new ArrayList<>());
                    }

                    // build list of existing product details
                    List<Product> existing = customer.getProducts() == null
                            ? Collections.emptyList()
                            : customer.getProducts();

                    String custType = customer.getCustomerType() == null ?
                            "PERSONAL" : customer.getCustomerType();
                    String custProfile = customer.getProfile() == null ?
                            "STANDARD" : customer.getProfile();

                    String pType = newProduct.getType();
                    String pSub = newProduct.getSubType();

                    // Count existing subtypes/types
                    long savingsCount = existing.stream()
                            .filter(
                                    p -> "ACCOUNT".equalsIgnoreCase(p.getType()) &&
                                            "SAVINGS".equalsIgnoreCase(p.getSubType()))
                            .count();
                    long currentCount = existing.stream()
                            .filter(
                                    p -> "ACCOUNT".equalsIgnoreCase(p.getType()) &&
                                            "CURRENT".equalsIgnoreCase(p.getSubType()))
                            .count();
                    long fixedCount = existing.stream()
                            .filter(
                                    p -> "ACCOUNT".equalsIgnoreCase(p.getType()) &&
                                            "FIXED_TERM".equalsIgnoreCase(p.getSubType()))
                            .count();
                    long personalLoanCount = existing.stream()
                            .filter(p ->
                                    "LOAN".equalsIgnoreCase(p.getType()) &&
                                            "PERSONAL_LOAN".equalsIgnoreCase(p.getSubType()))
                            .count();
                    boolean hasCreditCard = existing.stream()
                            .anyMatch(
                                    p -> "CREDIT_CARD".equalsIgnoreCase(p.getType()));

                    // RULES (local validation)
                    // Business cannot have savings or fixed-term accounts
                    if ("BUSINESS".equalsIgnoreCase(custType)
                            && "ACCOUNT".equalsIgnoreCase(pType)
                            && ("SAVINGS".equalsIgnoreCase(pSub) ||
                            "FIXED_TERM".equalsIgnoreCase(pSub))) {
                        return Mono.error(new IllegalArgumentException(
                                "Business customers cannot have savings or fixed-term accounts"));
                    }

                    // Personal limits: max 1 savings, max 1 current (interpretation)
                    if ("PERSONAL".equalsIgnoreCase(custType) &&
                            "ACCOUNT".equalsIgnoreCase(pType)) {
                        if ("SAVINGS".equalsIgnoreCase(pSub) && savingsCount >= 1) {
                            return Mono.error(
                                    new IllegalArgumentException(
                                            "Personal customer already has a savings account"));
                        }
                        if ("CURRENT".equalsIgnoreCase(pSub) && currentCount >= 1) {
                            return Mono.error(
                                    new IllegalArgumentException(
                                            "Personal customer already has a current account"));
                        }
                    }

                    // Loans: personal only 1 personal loan
                    if ("LOAN".equalsIgnoreCase(pType)
                            && "PERSONAL_LOAN".equalsIgnoreCase(pSub)
                            && "PERSONAL".equalsIgnoreCase(custType)
                            && personalLoanCount >= 1) {
                        return Mono.error(new IllegalArgumentException(
                                "Personal customer already has a personal loan"));
                    }

                    // VIP: personal with profile VIP requires credit card present to create SAVINGS
                    if ("VIP".equalsIgnoreCase(custProfile)
                            && "ACCOUNT".equalsIgnoreCase(pType)
                            && "SAVINGS".equalsIgnoreCase(pSub)
                            && !hasCreditCard) {
                        return Mono.error(
                                new IllegalArgumentException(
                                        "VIP personal must have a credit card" +
                                                " to create a VIP savings account"
                                ));
                    }

                    // PYME: business profile PYME requires credit card to create CURRENT (PYME)
                    if ("PYME".equalsIgnoreCase(custProfile)
                            && "BUSINESS".equalsIgnoreCase(custType)
                            && "ACCOUNT".equalsIgnoreCase(pType)
                            && "CURRENT".equalsIgnoreCase(pSub)
                            && !hasCreditCard) {
                        return Mono.error(new IllegalArgumentException(
                                "PYME must have a credit card to create the PYME current account"));
                    }

                    // Finally add product (avoid duplicates)
                    boolean already = existing.stream().anyMatch(
                            p -> p.getId().equals(newProduct.getId()));
                    if (!already) {
                        customer.getProducts().add(newProduct);
                    }
                    return customerRepository.save(customer).then();
                });
    }

    /**
     * Elimina un producto de la lista de productos de un cliente.
     *
     * - Si el cliente no existe, lanza excepción.
     * - Si la lista contiene el producto, se elimina y se guarda la entidad.
     *
     * @param customerId identificador del cliente
     * @param productId identificador del producto a eliminar
     * @return cliente con la lista de productos actualizada
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Override
    public Mono<Void> removeProduct(String customerId, String productId) {
        return customerRepository.findById(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(customer -> {
                    if (customer.getProducts() != null) {
                        // Remover el producto que tenga el ID igual al productId proporcionado
                        boolean removed = customer.getProducts().removeIf(product ->
                                product.getId().equals(productId));

                        if (removed) {
                            return customerRepository.save(customer);
                        }
                    }
                    return Mono.just(customer);
                })
                .then();
    }

    /**
     * Obtiene la lista de IDs de productos asociados a un cliente.
     *
     * - Si el cliente no existe, devuelve una lista vacía.
     *
     * @param customerId identificador del cliente
     * @return lista de IDs de productos
     */
    @Override
    public Flux<Product> getProductIds(String customerId) {
        return customerRepository.findById(customerId)
                .flatMapMany(customer -> {
                    if (customer.getProducts() != null) {
                        return Flux.fromIterable(customer.getProducts());
                    } else {
                        return Flux.empty();
                    }
                })
                .switchIfEmpty(Flux.empty());
    }
}
