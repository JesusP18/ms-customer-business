package com.customer.business;

import com.customer.business.api.ApiApiDelegate;
import com.customer.business.mapper.CustomerMapper;
import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.ProductIdRequest;
import com.customer.business.model.entity.Customer;
import com.customer.business.service.CustomerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación de {@link ApiApiDelegate} que actúa como puente entre la API REST y la capa de servicio.
 * 
 * Se encarga de:
 * - Recibir las peticiones HTTP.
 * - Validar y mapear DTOs (Request/Response).
 * - Llamar a la capa de negocio {@link CustomerService}.
 * - Retornar respuestas apropiadas con {@link ResponseEntity}.
 */
@Service
public class CustomerApiDelegateImpl implements ApiApiDelegate {

    private final CustomerService customerService;

    public CustomerApiDelegateImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    /**
     * Crea un nuevo cliente.
     *
     * @param customerRequest DTO de entrada con datos del cliente
     * @return ResponseEntity con el cliente creado y código 201 (CREATED)
     */
    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CustomerRequest customerRequest) {
        Customer customer = CustomerMapper.getCustomerofCustomerRequest(customerRequest);

        Customer saved = customerService.create(customer);
        CustomerResponse resp = CustomerMapper.getCustomerResponseOfCustomer(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    /**
     * Elimina un cliente por ID.
     *
     * - Si el cliente existe → 204 (NO CONTENT).
     * - Si no existe → 404 (NOT FOUND).
     *
     * @param id identificador del cliente
     * @return ResponseEntity vacío con el código correspondiente
     */
    @Override
    public ResponseEntity<Void> deleteCustomer(String id) {
        return customerService.findById(id)
                .map(c -> {
                    customerService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Obtiene todos los clientes.
     *
     * @return ResponseEntity con la lista de clientes y código 200 (OK)
     */
    @Override
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> list = customerService.findAll();
        List<CustomerResponse> responses = list.stream()
                .map(CustomerMapper::getCustomerResponseOfCustomer)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    /**
     * Busca un cliente por ID.
     *
     * - Si lo encuentra → 200 (OK).
     * - Si no lo encuentra → 404 (NOT FOUND).
     *
     * @param id identificador del cliente
     * @return ResponseEntity con el cliente o 404
     */
    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(String id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(CustomerMapper.getCustomerResponseOfCustomer(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualiza un cliente existente.
     *
     * - Si el cliente existe → 200 (OK) con datos actualizados.
     * - Si no existe → 404 (NOT FOUND).
     *
     * @param id identificador del cliente
     * @param customerRequest DTO con datos a actualizar
     * @return ResponseEntity con el cliente actualizado o 404
     */
    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(String id, CustomerRequest customerRequest) {
        Customer toUpdate = CustomerMapper.getCustomerofCustomerRequest(customerRequest);
        try {
            Customer updated = customerService.update(id, toUpdate);
            return ResponseEntity.ok(CustomerMapper.getCustomerResponseOfCustomer(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Obtiene los IDs de productos asociados a un cliente.
     *
     * - Si el cliente existe pero no tiene productos, devuelve lista vacía.
     * - Si el cliente no existe, igualmente retorna lista vacía (la lógica está en el service).
     *
     * @param id identificador del cliente
     * @return ResponseEntity con lista de IDs de productos
     */
    @Override
    public ResponseEntity<List<String>> getCustomerProducts(String id) {
        List<String> productIds = customerService.getProductIds(id);
        return ResponseEntity.ok(productIds);
    }

    /**
     * Asocia un producto a un cliente.
     *
     * - Valida que el request tenga un productId válido (no null, no vacío).
     * - Si el cliente existe, agrega el producto y devuelve 204 (NO CONTENT).
     * - Si el cliente no existe, devuelve 404 (NOT FOUND).
     * - Si el productId es inválido, devuelve 400 (BAD REQUEST).
     *
     * @param id identificador del cliente
     * @param productIdRequest DTO con el ID del producto
     * @return ResponseEntity vacío con el código correspondiente
     */
    @Override
    public ResponseEntity<Void> addProductToCustomer(String id, ProductIdRequest productIdRequest) {
        if (productIdRequest == null || productIdRequest.getProductId() == null || productIdRequest.getProductId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            customerService.addProduct(id, productIdRequest.getProductId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Elimina un producto asociado a un cliente.
     *
     * - Si el cliente existe, se elimina el producto y devuelve 204 (NO CONTENT).
     * - Si el cliente no existe, devuelve 404 (NOT FOUND).
     *
     * @param id identificador del cliente
     * @param productId identificador del producto
     * @return ResponseEntity vacío con el código correspondiente
     */
    @Override
    public ResponseEntity<Void> removeProductFromCustomer(String id, String productId) {
        try {
            customerService.removeProduct(id, productId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
