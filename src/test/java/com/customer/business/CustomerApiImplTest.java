package com.customer.business;

import com.customer.business.mapper.CustomerMapper;
import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.ProductRequest;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.service.CustomerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = CustomerApiImpl.class)
@Import(CustomerApiImpl.class)
class CustomerApiImplTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CustomerService customerService;

    @MockBean
    private CustomerMapper customerMapper;

    private CustomerRequest customerRequest;

    private CustomerResponse customerResponse;

    private Customer customerEntity;

    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        customerRequest = new CustomerRequest();
        customerRequest.setCustomerType(CustomerRequest.CustomerTypeEnum.PERSONAL);
        customerRequest.setFirstName("John");
        customerRequest.setLastName("Doe");
        customerRequest.setProfile(CustomerRequest.ProfileEnum.STANDARD);

        customerResponse = new CustomerResponse();
        customerResponse.setId("1");
        customerResponse.setCustomerType(CustomerResponse.CustomerTypeEnum.PERSONAL);
        customerResponse.setFirstName("John");
        customerResponse.setLastName("Doe");
        customerResponse.setProfile(CustomerResponse.ProfileEnum.STANDARD);

        customerEntity = new Customer();
        customerEntity.setId("1");
        customerEntity.setCustomerType("PERSONAL");
        customerEntity.setFirstName("John");
        customerEntity.setLastName("Doe");
        customerEntity.setProfile("STANDARD");

        productRequest = new ProductRequest();
        productRequest.setId("prod1");
        productRequest.setCategory(ProductRequest.CategoryEnum.LIABILITY);
        productRequest.setType(ProductRequest.TypeEnum.ACCOUNT);
        productRequest.setSubType(ProductRequest.SubTypeEnum.SAVINGS);
    }

    @Test
    void createCustomerShouldReturnCreated() {
        when(customerMapper.getCustomerofCustomerRequest(any())).thenReturn(customerEntity);
        when(customerService.create(any())).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.post()
                .uri("/api/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customerRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);

        verify(customerService).create(any());
    }

    @Test
    void getAllCustomersShouldReturnCustomers() {
        when(customerService.findAll()).thenReturn(Flux.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.get()
                .uri("/api/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(1)
                .contains(customerResponse);

        verify(customerService).findAll();
    }

    @Test
    void getCustomerByIdShouldReturnCustomer() {
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.get()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);

        verify(customerService).findById("1");
    }

    @Test
    void getCustomerByIdShouldReturnNotFound() {
        when(customerService.findById("1")).thenReturn(Mono.empty());

        webTestClient.get()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNotFound();

        verify(customerService).findById("1");
    }

    @Test
    void updateCustomerShouldReturnUpdatedCustomer() {
        when(customerMapper.getCustomerofCustomerRequest(any())).thenReturn(customerEntity);
        when(customerService.update(eq("1"), any())).thenReturn(Mono.just(customerEntity));
        when(customerMapper.getCustomerResponseOfCustomer(any())).thenReturn(customerResponse);

        webTestClient.put()
                .uri("/api/customers/1")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(customerRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .isEqualTo(customerResponse);

        verify(customerService).update(eq("1"), any());
    }

    @Test
    void deleteCustomerShouldReturnNoContent() {
        when(customerService.findById("1")).thenReturn(Mono.just(customerEntity));
        when(customerService.delete("1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNoContent();

        verify(customerService).delete("1");
    }

    @Test
    void deleteCustomerShouldReturnNotFound() {
        when(customerService.findById("1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1")
                .exchange()
                .expectStatus().isNotFound();

        verify(customerService, never()).delete("1");
    }

    @Test
    void getCustomerProductsShouldReturnProducts() {
        Product product = new Product("prod1", "LIABILITY", "ACCOUNT", "SAVINGS");
        when(customerService.getProductIds("1")).thenReturn(Flux.just(product));

        webTestClient.get()
                .uri("/api/customers/1/products")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("prod1");

        verify(customerService).getProductIds("1");
    }

    @Test
    void addProductToCustomerShouldReturnNoContent() {
        Product productEntity = new Product("prod1", "LIABILITY", "ACCOUNT", "SAVINGS");
        when(customerService.addProduct(eq("1"), any())).thenReturn(Mono.empty());

        webTestClient.post()
                .uri("/api/customers/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequest)
                .exchange()
                .expectStatus().isNoContent();

        verify(customerService).addProduct(eq("1"), any());
    }

    @Test
    void addProductToCustomerShouldReturnBadRequest() {
        productRequest.setId(null);

        webTestClient.post()
                .uri("/api/customers/1/products")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(productRequest)
                .exchange()
                .expectStatus().isBadRequest();

        verify(customerService, never()).addProduct(any(), any());
    }

    @Test
    void removeProductFromCustomerShouldReturnNoContent() {
        when(customerService.removeProduct("1", "prod1")).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/customers/1/products/prod1")
                .exchange()
                .expectStatus().isNoContent();

        verify(customerService).removeProduct("1", "prod1");
    }

    @Test
    void removeProductFromCustomerShouldReturnNotFound() {
        when(customerService.removeProduct("1", "prod1"))
                .thenReturn(Mono.error(new IllegalArgumentException("Customer not found")));

        webTestClient.delete()
                .uri("/api/customers/1/products/prod1")
                .exchange()
                .expectStatus().isNotFound();

        verify(customerService).removeProduct("1", "prod1");
    }
}