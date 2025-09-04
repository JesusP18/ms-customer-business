package com.customer.business.mapper;

import com.customer.business.model.CustomerCreateRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.CustomerUpdateRequest;
import com.customer.business.model.entity.Customer;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    void getCustomerofCustomerCreateRequestShouldReturnNullWhenRequestIsNull() {
        assertNull(mapper.getCustomerofCustomerCreateRequest(null));
    }

    @Test
    void getCustomerofCustomerCreateRequestShouldMapBasicFields() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setProfile(CustomerCreateRequest.ProfileEnum.STANDARD);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setBusinessName("Business");
        request.setDni("12345678");
        request.setRuc("12345678901");
        request.setAddress("Address");
        request.setPhone("123456789");
        request.setEmail("john@example.com");

        Customer result = mapper.getCustomerofCustomerCreateRequest(request);

        assertEquals("PERSONAL", result.getCustomerType());
        assertEquals("STANDARD", result.getProfile());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Business", result.getBusinessName());
        assertEquals("12345678", result.getDni());
        assertEquals("12345678901", result.getRuc());
        assertEquals("Address", result.getAddress());
        assertEquals("123456789", result.getPhone());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getCustomerResponseOfCustomerShouldReturnNullWhenCustomerIsNull() {
        assertNull(mapper.getCustomerResponseOfCustomer(null));
    }

    @Test
    void getCustomerResponseOfCustomerShouldMapBasicFields() {
        Customer customer = new Customer();
        customer.setId("1");
        customer.setCustomerType("PERSONAL");
        customer.setProfile("STANDARD");
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setBusinessName("Business");
        customer.setDni("12345678");
        customer.setRuc("12345678901");
        customer.setAddress("Address");
        customer.setPhone("123456789");
        customer.setEmail("john@example.com");

        CustomerResponse result = mapper.getCustomerResponseOfCustomer(customer);

        assertEquals("1", result.getId());
        assertEquals(
                CustomerResponse.CustomerTypeEnum.PERSONAL, result.getCustomerType()
        );
        assertEquals(CustomerResponse.ProfileEnum.STANDARD, result.getProfile());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("Business", result.getBusinessName());
        assertEquals("12345678", result.getDni());
        assertEquals("12345678901", result.getRuc());
        assertEquals("Address", result.getAddress());
        assertEquals("123456789", result.getPhone());
        assertEquals("john@example.com", result.getEmail());
    }

    @Test
    void getCustomerFromUpdateRequestShouldUpdateExistingCustomer() {
        Customer existingCustomer = new Customer();
        existingCustomer.setFirstName("OldName");
        existingCustomer.setLastName("OldLastName");
        existingCustomer.setBusinessName("OldBusiness");
        existingCustomer.setAddress("OldAddress");
        existingCustomer.setPhone("OldPhone");
        existingCustomer.setEmail("old@example.com");

        CustomerUpdateRequest updateRequest = new CustomerUpdateRequest();
        updateRequest.setFirstName("NewName");
        updateRequest.setLastName("NewLastName");
        updateRequest.setBusinessName("NewBusiness");
        updateRequest.setAddress("NewAddress");
        updateRequest.setPhone("NewPhone");
        updateRequest.setEmail("new@example.com");

        Customer result = mapper.getCustomerFromUpdateRequest(
                updateRequest, existingCustomer
        );

        assertEquals("NewName", result.getFirstName());
        assertEquals("NewLastName", result.getLastName());
        assertEquals("NewBusiness", result.getBusinessName());
        assertEquals("NewAddress", result.getAddress());
        assertEquals("NewPhone", result.getPhone());
        assertEquals("new@example.com", result.getEmail());
    }
}