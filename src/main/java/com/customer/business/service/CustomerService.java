package com.customer.business.service;

import com.customer.business.model.entity.Customer;
import com.customer.business.repository.CustomerRepository;
import org.springframework.stereotype.Service;

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
}