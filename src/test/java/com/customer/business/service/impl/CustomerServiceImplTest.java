package com.customer.business.service.impl;

import com.customer.business.model.entity.Customer;
import com.customer.business.model.entity.Product;
import com.customer.business.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CustomerServiceImplTest {

    @Mock
    private CustomerRepository repository;

    @InjectMocks
    private CustomerServiceImpl service;

    @Test
    void findAllShouldReturnAllCustomers() {
        Customer customer1 = new Customer();
        customer1.setId("1");
        Customer customer2 = new Customer();
        customer2.setId("2");

        when(repository.findAll()).thenReturn(Flux.just(customer1, customer2));

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
    void findByIdShouldReturnEmptyWhenNotExists() {
        when(repository.findById("1")).thenReturn(Mono.empty());

        StepVerifier.create(service.findById("1"))
                .verifyComplete();
    }

    @Test
    void createShouldSaveCustomer() {
        Customer customer = new Customer();
        customer.setFirstName("John");
        when(repository.save(customer)).thenReturn(Mono.just(customer));

        StepVerifier.create(service.create(customer))
                .expectNext(customer)
                .verifyComplete();
    }

    @Test
    void updateShouldUpdateExistingCustomer() {
        Customer existing = new Customer();
        existing.setId("1");
        existing.setFirstName("Old Name");

        Customer update = new Customer();
        update.setFirstName("New Name");

        when(repository.findById("1")).thenReturn(Mono.just(existing));
        when(repository.save(any())).thenReturn(Mono.just(existing));

        StepVerifier.create(service.update("1", update))
                .expectNextMatches(updated -> "New Name".equals(updated.getFirstName()))
                .verifyComplete();
    }

    @Test
    void updateShouldErrorWhenCustomerNotFound() {
        Customer update = new Customer();
        when(repository.findById("1")).thenReturn(Mono.empty());

        StepVerifier.create(service.update("1", update))
                .verifyError(IllegalArgumentException.class);
    }

    @Test
    void deleteShouldDeleteCustomer() {
        when(repository.deleteById("1")).thenReturn(Mono.empty());

        StepVerifier.create(service.delete("1"))
                .verifyComplete();

        verify(repository).deleteById("1");
    }

    @Test
    void addProductShouldAddProductToCustomer() {
        Customer customer = new Customer();
        customer.setId("1");
        customer.setCustomerType("PERSONAL");
        customer.setProfile("STANDARD");
        customer.setProducts(new ArrayList<>());

        Product product = new Product();
        product.setId("prod1");
        product.setType("ACCOUNT");
        product.setSubType("SAVINGS");

        when(repository.findById("1")).thenReturn(Mono.just(customer));
        when(repository.save(any())).thenReturn(Mono.just(customer));

        StepVerifier.create(service.addProduct("1", product))
                .verifyComplete();

        verify(repository).save(customer);
    }

    @Test
    void removeProductShouldRemoveProductFromCustomer() {
        Customer customer = new Customer();
        customer.setId("1");
        Product product = new Product("prod1", "LIABILITY", "ACCOUNT", "SAVINGS");
        customer.setProducts(new ArrayList<>(List.of(product)));

        when(repository.findById("1")).thenReturn(Mono.just(customer));
        when(repository.save(any())).thenReturn(Mono.just(customer));

        StepVerifier.create(service.removeProduct("1", "prod1"))
                .verifyComplete();

        verify(repository).save(customer);
    }

    @Test
    void getProductIdsShouldReturnProducts() {
        Customer customer = new Customer();
        customer.setId("1");
        Product product = new Product("prod1", "LIABILITY", "ACCOUNT", "SAVINGS");
        customer.setProducts(List.of(product));

        when(repository.findById("1")).thenReturn(Mono.just(customer));

        StepVerifier.create(service.getProductIds("1"))
                .expectNext(product)
                .verifyComplete();
    }

    @Test
    void getProductIdsShouldReturnEmptyWhenNoProducts() {
        Customer customer = new Customer();
        customer.setId("1");
        customer.setProducts(null);

        when(repository.findById("1")).thenReturn(Mono.just(customer));

        StepVerifier.create(service.getProductIds("1"))
                .verifyComplete();
    }

    @Test
    void addProductShouldValidateBusinessRules() {
        Customer customer = new Customer();
        customer.setCustomerType("BUSINESS");
        customer.setProducts(List.of());

        Product product = new Product();
        product.setId("prod1");
        product.setType("ACCOUNT");
        product.setSubType("SAVINGS");

        when(repository.findById("1")).thenReturn(Mono.just(customer));

        StepVerifier.create(service.addProduct("1", product))
                .verifyError(IllegalArgumentException.class);
    }
}