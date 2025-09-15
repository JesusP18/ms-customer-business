package com.customer.business.service.impl;

import com.customer.business.exception.ResourceNotFoundException;
import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.repository.CustomerRepository;
import com.customer.business.resilience.ResilienceOperatorService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @Mock
    private WebClient productWebClient;

    @Mock
    private ResilienceOperatorService resilienceOperatorService;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @InjectMocks
    private CustomerServiceImpl service;

    @Test
    void findAllShouldReturnAllCustomers() {
        Customer customer1 = new Customer();
        customer1.setId("1");
        Customer customer2 = new Customer();
        customer2.setId("2");

        when(repository.findAll()).thenReturn(
                Flux.just(customer1, customer2)
        );

        StepVerifier.create(service.findAll())
                .expectNext(customer1)
                .expectNext(customer2)
                .verifyComplete();
    }

    @Test
    void findByIdShouldReturnCustomerWhenExists() {
        Customer customer = new Customer();
        customer.setId("1");
        when(repository.findById("1")).thenReturn(Mono.just(customer));

        StepVerifier.create(service.findById("1"))
                .expectNext(customer)
                .verifyComplete();
    }

    @Test
    void findByIdShouldErrorWhenNotExists() {
        when(repository.findById("1")).thenReturn(Mono.empty());

        StepVerifier.create(service.findById("1"))
                .verifyError(ResourceNotFoundException.class);
    }

    @Test
    void createShouldSaveCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        customer.setDni("11111111");
        when(repository.existsByDni(customer.getDni())).thenReturn(Mono.just(false));
        when(repository.save(customer)).thenReturn(Mono.just(customer));

        StepVerifier.create(service.create(customer))
                .expectNext(customer)
                .verifyComplete();
    }

    @Test
    void deleteShouldDeleteCustomer() {
        Customer customer = new Customer();
        customer.setId("1");

        when(repository.findById("1")).thenReturn(Mono.just(customer));
        when(repository.deleteById("1")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("1"))
                .verifyComplete();

        verify(repository).deleteById("1");
    }

    void getProductsShouldReturnProducts() {
        Customer customer = new Customer();
        customer.setId("1");
        Product product = new Product("1", "LIABILITY", "ACCOUNT", "SAVINGS");

        when(repository.findById("1")).thenReturn(Mono.just(customer));
        when(productWebClient.get()).thenReturn(requestHeadersUriSpec);

        // Stubbing específico para la llamada esperada
        when(requestHeadersUriSpec.uri(
                eq("/{customerId}"), any(String.class))
        ).thenReturn(requestHeadersSpec);

        when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToFlux(Product.class)).thenReturn(Flux.just(product));
        when(resilienceOperatorService.withCircuitBreaker(
                any(Flux.class), any(CircuitBreaker.class))
        ).thenReturn(Flux.just(product));

        StepVerifier.create(service.getProducts("1"))
                .expectNext(product)
                .verifyComplete();
    }
}