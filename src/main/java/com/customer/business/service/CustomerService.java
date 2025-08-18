package com.customer.business.service;

import com.customer.business.model.entity.Customer;
import com.customer.business.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerService {
    private final CustomerRepository repository;

    public CustomerService(CustomerRepository repo) { this.repository = repo; }

    public List<Customer> findAll() { return repository.findAll(); }
    public Optional<Customer> findById(String id) { return repository.findById(id); }
    public Customer create(Customer c) { return repository.save(c); }
    public Customer update(String id, Customer c) {
        if (!repository.existsById(id)) throw new IllegalArgumentException("Customer not found with id: " + id);
        c.setId(id);
        return repository.save(c);
    }
    public void delete(String id) { repository.deleteById(id); }

    public Customer addProduct(String customerId, String productId) {
        Customer c = repository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (c.getProductIds() == null) c.setProductIds(new ArrayList<>());
        if (!c.getProductIds().contains(productId)) {
            c.getProductIds().add(productId);
            repository.save(c);
        }
        return c;
    }

    public Customer removeProduct(String customerId, String productId) {
        Customer c = repository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found"));
        if (c.getProductIds() != null && c.getProductIds().remove(productId)) {
            repository.save(c);
        }
        return c;
    }

    public List<String> getProductIds(String customerId) {
        return repository.findById(customerId)
                .map(Customer::getProductIds)
                .orElse(Collections.emptyList());
    }

}