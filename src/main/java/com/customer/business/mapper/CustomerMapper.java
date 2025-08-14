package com.customer.business.mapper;

import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.entity.Customer;
import lombok.NoArgsConstructor;

import java.util.ArrayList;

/**
 * Mapper entre:
 * - DTOs generados por OpenAPI: CustomerRequest, CustomerResponse
 * - Entity/documento Mongo: com.customer.business.model.entity.Customer
 *
 * Nota: CustomerRequest/Response vienen con enums internos CustomerTypeEnum (PERSONAL, EMPRESA).
 *       Entity usa un campo String customerType y Date createdAt/updatedAt.
 */
@NoArgsConstructor
public final class CustomerMapper {

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
        customer.setProducts(request.getProducts() == null ? new ArrayList<>() : new ArrayList<>(request.getProducts()));
        return customer;
    }

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

        if (customer.getProducts() != null) {
            response.setProducts(new ArrayList<>(customer.getProducts()));
        } else {
            response.setProducts(new ArrayList<>());
        }
        return response;
    }
}
