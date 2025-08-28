package com.customer.business.service;

import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.repository.CustomerRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Servicio que encapsula la lógica de negocio relacionada con los clientes.
 * Se comunica con el repositorio {@link CustomerRepository} para persistencia en MongoDB.
 */
public interface CustomerService {

    public Flux<Customer> findAll();

    /**
     * Busca un cliente por su identificador.
     *
     * @param customerId identificador del cliente
     * @return Optional con el cliente si existe, vacío si no
     */
    public Mono<Customer> findById(String customerId);

    /**
     * Crea un nuevo cliente en la base de datos.
     *
     * @param customer entidad del cliente a crear
     * @return cliente persistido
     */
    public Mono<Customer> create(Customer customer);

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
    public Mono<Customer> update(String customerId, Customer customer);

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param customerId identificador del cliente a eliminar
     */
    public Mono<Void> delete(String customerId);

    /**
     * Agrega un producto a un cliente en específico
     *
     * @param customerId identificador del cliente
     * @param product identificador del producto a agregar
     */
    public Mono<Void> addProduct(String customerId, Product product);

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
    public Mono<Void> removeProduct(String customerId, String productId);

    /**
     * Obtiene la lista de IDs de productos asociados a un cliente.
     *
     * - Si el cliente no existe, devuelve una lista vacía.
     *
     * @param customerId identificador del cliente
     * @return lista de IDs de productos
     */
    public Flux<Product> getProductIds(String customerId);
}