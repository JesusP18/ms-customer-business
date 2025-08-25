package com.customer.business;

import com.customer.business.api.ApiApiDelegate;
import com.customer.business.mapper.CustomerMapper;
import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.ProductIdRequest;
import com.customer.business.model.entity.Customer;
import com.customer.business.service.CustomerService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
@Slf4j
@AllArgsConstructor
@Component
public class CustomerApiDelegateImpl implements ApiApiDelegate {

    private final CustomerService customerService;

    private final CustomerMapper customerMapper;

    /**
     * Crea un nuevo cliente.
     *
     * @param customerRequest DTO de entrada con datos del cliente
     * @return ResponseEntity con el cliente creado y código 201 (CREATED)
     */
    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CustomerRequest customerRequest) {
        log.info("[CREATE_CUSTOMER] Solicitud para crear un nuevo cliente: {}", customerRequest);

        Customer customer = customerMapper.getCustomerofCustomerRequest(customerRequest);

        Customer saved = customerService.create(customer);

        log.debug("[CREATE_CUSTOMER] Cliente guardado en base de datos: {}", saved);
        CustomerResponse resp = customerMapper.getCustomerResponseOfCustomer(saved);

        log.info("[CREATE_CUSTOMER] Cliente creado exitosamente con ID={}", resp.getId());
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
        log.info("[DELETE_CUSTOMER] Solicitud para eliminar cliente con ID={}", id);

        return customerService.findById(id)
                .map(customer -> {
                    customerService.delete(id);
                    log.info("[DELETE_CUSTOMER] Cliente eliminado con éxito. ID={}", id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElseGet(() -> {
                    log.warn("[DELETE_CUSTOMER] No se encontró cliente con ID={} para eliminación", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Obtiene todos los clientes.
     *
     * @return ResponseEntity con la lista de clientes y código 200 (OK)
     */
    @Override
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        log.info("[GET_ALL_CUSTOMERS] Solicitud para obtener todos los clientes");
        List<Customer> list = customerService.findAll();

        log.debug("[GET_ALL_CUSTOMERS] Clientes encontrados en BD: {}", list);
        List<CustomerResponse> responses = list.stream()
                .map(customerMapper::getCustomerResponseOfCustomer)
                .collect(Collectors.toList());

        log.info("[GET_ALL_CUSTOMERS] Total de clientes devueltos: {}", responses.size());
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
        log.info("[GET_ALL_CUSTOMER_BY_ID] Solicitud para obtener cliente por ID={}", id);

        return customerService.findById(id)
                .map(customer -> {
                    log.info("[GET_ALL_CUSTOMER_BY_ID] Cliente encontrado con ID={}", id);
                    return ResponseEntity.ok(customerMapper.getCustomerResponseOfCustomer(customer));
                })
                .orElseGet(() -> {
                    log.warn("[GET_ALL_CUSTOMER_BY_ID] No se encontró cliente con ID={}", id);
                    return ResponseEntity.notFound().build();
                });
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
        log.info("[UPDATE_CUSTOMER] Solicitud de actualización para cliente ID={} con datos: {}", id, customerRequest);

        Customer toUpdate = customerMapper.getCustomerofCustomerRequest(customerRequest);
        try {
            Customer updated = customerService.update(id, toUpdate);
            log.info("[UPDATE_CUSTOMER] Cliente actualizado con éxito. ID={}", updated.getId());
            return ResponseEntity.ok(customerMapper.getCustomerResponseOfCustomer(updated));
        } catch (IllegalArgumentException ex) {
            log.error("[UPDATE_CUSTOMER] Error al actualizar cliente ID={}: {}", id, ex.getMessage());
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
        log.info("[GET_CUSTOMER_PRODUCTS] Solicitud para obtener productos de cliente ID={}", id);

        List<String> productIds = customerService.getProductIds(id);
        log.info("[GET_CUSTOMER_PRODUCTS] Cliente ID={} tiene {} productos asociados", id, productIds.size());
        log.debug("[GET_CUSTOMER_PRODUCTS] Lista de productos: {}", productIds);

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
        log.info("[ADD_PRODUCTS_CUSTOMER] Solicitud para asociar producto a cliente ID={}. Request={}", id, productIdRequest);

        if (productIdRequest == null || productIdRequest.getProductId() == null || productIdRequest.getProductId().isBlank()) {
            log.warn("[ADD_PRODUCTS_CUSTOMER] Request inválido para asociar producto a cliente ID={}: {}", id, productIdRequest);
            return ResponseEntity.badRequest().build();
        }
        try {
            customerService.addProduct(id, productIdRequest.getProductId());
            log.info("[ADD_PRODUCTS_CUSTOMER] Producto {} agregado exitosamente al cliente ID={}", productIdRequest.getProductId(), id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            log.error("[ADD_PRODUCTS_CUSTOMER] Error al agregar producto {} a cliente ID={}: {}", productIdRequest.getProductId(), id, ex.getMessage());
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
        log.info("[REMOVE_PRODUCT_FROM_CUSTOMER] Solicitud para eliminar producto {} del cliente ID={}", productId, id);

        try {
            customerService.removeProduct(id, productId);
            log.info("[REMOVE_PRODUCT_FROM_CUSTOMER] Producto {} eliminado del cliente ID={}", productId, id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            log.error("[REMOVE_PRODUCT_FROM_CUSTOMER] Error al eliminar producto {} del cliente ID={}: {}", productId, id, ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
