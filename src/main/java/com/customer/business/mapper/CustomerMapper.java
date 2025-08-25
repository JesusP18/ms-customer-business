package com.customer.business.mapper;

import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.ProductResponse;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
@Component
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
    public Customer getCustomerofCustomerRequest(CustomerRequest request) {
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

        // Convertir List<ProductRequest> a List<Product>
        List<Product> productList = new ArrayList<>();
        if (request.getProducts() != null) {
            productList = request.getProducts().stream()
                    .map(productRequest -> new Product(productRequest.getId()))
                    .collect(Collectors.toList());
        }
        customer.setProducts(productList);

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
    public CustomerResponse getCustomerResponseOfCustomer(Customer customer) {
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

        // Convertir List<Product> a List<ProductResponse>
        List<ProductResponse> productResponses = new ArrayList<>();
        if (customer.getProducts() != null) {
            productResponses = customer.getProducts().stream()
                    .map(product -> {
                        ProductResponse productResponse = new ProductResponse();
                        productResponse.setId(product.getId());
                        return productResponse;
                    })
                    .collect(Collectors.toList());
        }
        response.setProducts(productResponses);

        return response;
    }
}