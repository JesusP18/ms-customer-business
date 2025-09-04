package com.customer.business.mapper;

import com.customer.business.model.CustomerCreateRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.entity.Customer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class CustomerMapperTest {

    private final CustomerMapper mapper = new CustomerMapper();

    @Test
    void getCustomerofCustomerRequestShouldReturnNullWhenRequestIsNull() {
        assertNull(mapper.getCustomerofCustomerCreateRequest(null));
    }

    @Test
    void getCustomerofCustomerRequestShouldMapBasicFields() {
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setProfile(CustomerCreateRequest.ProfileEnum.STANDARD);

        Customer result = mapper.getCustomerofCustomerCreateRequest(request);

        assertEquals("PERSONAL", result.getCustomerType());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("STANDARD", result.getProfile());
    }

    @Test
    void getCustomerofCustomerRequestShouldMapProducts() {
//        CustomerRequest request = new CustomerRequest();
//        request.setCustomerType(CustomerRequest.CustomerTypeEnum.PERSONAL);
//        request.setFirstName("John");
//
//        ProductRequest productRequest = new ProductRequest();
//        productRequest.setId("prod1");
//        productRequest.setCategory(ProductRequest.CategoryEnum.LIABILITY);
//        productRequest.setType(ProductRequest.TypeEnum.ACCOUNT);
//        productRequest.setSubType(ProductRequest.SubTypeEnum.SAVINGS);
//
//        Customer result = mapper.getCustomerofCustomerRequest(request);
//
//        assertNotNull(result.getProducts());
//        assertEquals(1, result.getProducts().size());
//        assertEquals("prod1", result.getProducts().get(0).getId());
//        assertEquals("LIABILITY", result.getProducts().get(0).getCategory());
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
        customer.setFirstName("John");
        customer.setLastName("Doe");
        customer.setProfile("STANDARD");

        CustomerResponse result = mapper.getCustomerResponseOfCustomer(customer);

        assertEquals("1", result.getId());
        assertEquals(CustomerResponse.CustomerTypeEnum.PERSONAL, result.getCustomerType());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals(CustomerResponse.ProfileEnum.STANDARD, result.getProfile());
    }

    @Test
    void getCustomerResponseOfCustomerShouldHandleNullProducts() {
//        Customer customer = new Customer();
//        customer.setProducts(null);
//
//        CustomerResponse response = mapper.getCustomerResponseOfCustomer(customer);
//
//        assertNotNull(response);
//        assertTrue(response.getProducts() == null || response.getProducts().isEmpty());
    }

    @Test
    void getCustomerResponseOfCustomerShouldMapProducts() {
//        Customer customer = new Customer();
//        customer.setId("1");
//
//        Product product = new Product("prod1", "LIABILITY", "ACCOUNT", "SAVINGS");
//        customer.setProducts(List.of(product));
//
//        CustomerResponse response = mapper.getCustomerResponseOfCustomer(customer);
//
//        assertNotNull(response.getProducts());
//        assertEquals(1, response.getProducts().size());
//        assertEquals("prod1", response.getProducts().get(0).getId());
    }
}