package com.customer.business.mapper;

import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.entity.Customer;
import lombok.NoArgsConstructor;

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

    public static Customer getCustomerofCustomerRequest(CustomerRequest req) {
        if (req == null) return null;

        Customer e = new Customer();

        e.setId(null);

        if (req.getCustomerType() != null) {
            e.setCustomerType(req.getCustomerType().toString());
        } else {
            e.setCustomerType(null);
        }

        e.setFirstName(req.getFirstName());
        e.setLastName(req.getLastName());
        e.setBusinessName(req.getBusinessName());
        e.setDni(req.getDni());
        e.setRuc(req.getRuc());
        e.setAddress(req.getAddress());
        e.setPhone(req.getPhone());
        e.setEmail(req.getEmail());
        return e;
    }

    public static CustomerResponse getCustomerResponseOfCustomer(Customer e) {
        if (e == null) return null;

        CustomerResponse r = new CustomerResponse();
        r.setId(e.getId());

        if (e.getCustomerType() != null) {
            try {
                r.setCustomerType(CustomerResponse.CustomerTypeEnum.fromValue(e.getCustomerType()));
            } catch (IllegalArgumentException ex) {
                r.setCustomerType(null);
            }
        } else {
            r.setCustomerType(null);
        }

        r.setFirstName(e.getFirstName());
        r.setLastName(e.getLastName());
        r.setBusinessName(e.getBusinessName());
        r.setDni(e.getDni());
        r.setRuc(e.getRuc());
        r.setAddress(e.getAddress());
        r.setPhone(e.getPhone());
        r.setEmail(e.getEmail());

        return r;
    }
}
