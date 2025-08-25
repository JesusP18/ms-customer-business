package com.customer.business.service.impl;

import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.repository.CustomerRepository;
import com.customer.business.service.CustomerService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Servicio que encapsula la lógica de negocio relacionada con los clientes.
 * Se comunica con el repositorio {@link CustomerRepository} para persistencia en MongoDB.
 */
@AllArgsConstructor
@Service
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository repository;

    /**
     * Obtiene la lista de todos los clientes en la base de datos.
     *
     * @return lista de clientes
     */
    @Override
    public Flux<Customer> findAll() { return repository.findAll(); }

    /**
     * Busca un cliente por su identificador.
     *
     * @param customerId identificador del cliente
     * @return Optional con el cliente si existe, vacío si no
     */
    @Override
    public Mono<Customer> findById(String customerId) { return repository.findById(customerId); }

    /**
     * Crea un nuevo cliente en la base de datos.
     *
     * @param customer entidad del cliente a crear
     * @return cliente persistido
     */
    @Override
    public Mono<Customer> create(Customer customer) { return repository.save(customer); }

    /**
     * Actualiza un cliente existente.
     *
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
        return repository.findById(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(existing -> {
                    existing.setCustomerType(customer.getCustomerType());
                    existing.setFirstName(customer.getFirstName());
                    existing.setLastName(customer.getLastName());
                    existing.setBusinessName(customer.getBusinessName());
                    existing.setDni(customer.getDni());
                    existing.setRuc(customer.getRuc());
                    existing.setAddress(customer.getAddress());
                    existing.setPhone(customer.getPhone());
                    existing.setEmail(customer.getEmail());
                    if (customer.getProducts() != null) {
                        existing.setProducts(customer.getProducts());
                    }
                    return repository.save(existing);
                });
    }

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param customerId identificador del cliente a eliminar
     */
    @Override
    public Mono<Void> delete(String customerId) { return repository.deleteById(customerId); }

    /**
     * Agrega un producto a la lista de productos de un cliente.
     *
     * - Si el cliente no existe, lanza excepción.
     * - Si la lista de productos está vacía, se inicializa.
     * - Evita duplicados: solo agrega el producto si no está ya presente.
     *
     * @param customerId identificador del cliente
     * @param productId identificador del producto a agregar
     * @return cliente con la lista de productos actualizada
     * @throws IllegalArgumentException si el cliente no existe
     */
    @Override
    public Mono<Void> addProduct(String customerId, String productId) {
        return repository.findById(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(c -> {
                    if (c.getProducts() == null) {
                        c.setProducts(new ArrayList<>());
                    }

                    // Crear un nuevo objeto Product con el ID proporcionado
                    Product newProduct = new Product();
                    newProduct.setId(productId);

                    // Evitar duplicados verificando si ya existe un producto con este ID
                    boolean productExists = c.getProducts().stream()
                            .anyMatch(p -> p.getId().equals(productId));

                    if (!productExists) {
                        c.getProducts().add(newProduct);
                    }

                    return repository.save(c);
                })
                .then(); // devuelve Mono<Void>
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
        return repository.findById(customerId)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Customer not found")))
                .flatMap(customer -> {
                    if (customer.getProducts() != null) {
                        // Remover el producto que tenga el ID igual al productId proporcionado
                        boolean removed = customer.getProducts().removeIf(product ->
                                product.getId().equals(productId));

                        if (removed) {
                            return repository.save(customer);
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
        return repository.findById(customerId)
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
