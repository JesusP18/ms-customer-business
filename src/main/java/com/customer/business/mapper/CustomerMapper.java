package com.customer.business.mapper;

import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.entity.Customer;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * Mapper entre:
 * - DTOs generados por OpenAPI: CustomerRequest, CustomerResponse
 * - Entidad de base de datos: Customer
 *
 * Nota: 
 * - CustomerRequest/Response vienen con enums internos CustomerTypeEnum (PERSONAL, EMPRESA).
 * - La entidad usa un campo String customerType para simplificar persistencia.
 */
@NoArgsConstructor
public final class CustomerMapper {

    /**
     * Convierte un objeto {@link CustomerRequest} (DTO recibido en la API) 
     * en un objeto {@link Customer} (entidad de base de datos).
     *
     * - El campo "customerType" del request (enum) se transforma a String.
     * - Los campos nulos son tratados para evitar NullPointerExceptions.
     * - Se inicializa la lista de productIds si no viene en el request.
     *
     * @param request DTO recibido en la API
     * @return entidad Customer lista para ser persistida
     */
    public static Customer getCustomerofCustomerRequest(CustomerRequest request) {
        if (request == null) return null;

        Customer customer = new Customer();

        customer.setId(null);

        if (request.getCustomerType() != null) {
            customer.setCustomerType(request.getCustomerType().toString());
        } else {
            customer.setCustomerType(null);
        }

        customer.setFirstName(request.getFirstName());
        customer.setLastName(request.getLastName());
        customer.setBusinessName(request.getBusinessName());
        customer.setDni(request.getDni());
        customer.setRuc(request.getRuc());
        customer.setAddress(request.getAddress());
        customer.setPhone(request.getPhone());
        customer.setEmail(request.getEmail());
        customer.setProductIds(request.getProductIds() == null ? new ArrayList<>() : new ArrayList<>(request.getProductIds()));
        return customer;
    }

    /**
     * Convierte un objeto {@link Customer} (entidad de base de datos)
     * en un objeto {@link CustomerResponse} (DTO para respuesta en la API).
     *
     * - El campo "customerType" (String en BD) se transforma en el enum esperado por la API.
     * - Si el valor de "customerType" no corresponde con el enum, se setea como null.
     * - La lista de productIds se copia para no exponer referencias mutables.
     *
     * @param customer entidad Customer proveniente de la BD
     * @return DTO CustomerResponse para enviar en la respuesta de la API
     */
    public static CustomerResponse getCustomerResponseOfCustomer(Customer customer) {
        if (customer == null) return null;

        CustomerResponse response = new CustomerResponse();
        response.setId(customer.getId());

        if (customer.getCustomerType() != null) {
            try {
                response.setCustomerType(CustomerResponse.CustomerTypeEnum.fromValue(customer.getCustomerType()));
            } catch (IllegalArgumentException ex) {
                response.setCustomerType(null);
            }
        } else {
            response.setCustomerType(null);
        }

        response.setFirstName(customer.getFirstName());
        response.setLastName(customer.getLastName());
        response.setBusinessName(customer.getBusinessName());
        response.setDni(customer.getDni());
        response.setRuc(customer.getRuc());
        response.setAddress(customer.getAddress());
        response.setPhone(customer.getPhone());
        response.setEmail(customer.getEmail());

        if (customer.getProductIds() != null) {
            response.setProductIds(new ArrayList<>(customer.getProductIds()));
        } else {
            response.setProductIds(new ArrayList<>());
        }
        return response;
    }
}
