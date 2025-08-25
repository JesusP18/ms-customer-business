package com.customer.business.service;

import com.customer.business.model.entity.Customer;
import com.customer.business.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

/**
 * Servicio que encapsula la lógica de negocio relacionada con los clientes.
 * Se comunica con el repositorio {@link CustomerRepository} para persistencia en MongoDB.
 */
public interface CustomerService {

    public List<Customer> findAll();

    /**
     * Busca un cliente por su identificador.
     *
     * @param id identificador del cliente
     * @return Optional con el cliente si existe, vacío si no
     */
    public Optional<Customer> findById(String id);

    /**
     * Crea un nuevo cliente en la base de datos.
     *
     * @param c entidad del cliente a crear
     * @return cliente persistido
     */
    public Customer create(Customer c);

    /**
     * Actualiza un cliente existente.
     *
     * - Si el cliente no existe, lanza una excepción.
     * - El ID del cliente se fuerza para coincidir con el recibido en el parámetro.
     *
     * @param id identificador del cliente
     * @param c datos a actualizar
     * @return cliente actualizado
     * @throws IllegalArgumentException si el cliente no existe
     */
    public Customer update(String id, Customer c);

    /**
     * Elimina un cliente de la base de datos.
     *
     * @param id identificador del cliente a eliminar
     */
    public void delete(String id);

    public Customer addProduct(String customerId, String productId);

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
    public Customer removeProduct(String customerId, String productId);

    /**
     * Obtiene la lista de IDs de productos asociados a un cliente.
     *
     * - Si el cliente no existe, devuelve una lista vacía.
     *
     * @param customerId identificador del cliente
     * @return lista de IDs de productos
     */
    public List<String> getProductIds(String customerId);

}