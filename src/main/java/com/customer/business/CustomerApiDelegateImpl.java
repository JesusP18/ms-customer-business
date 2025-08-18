package com.customer.business;

import com.customer.business.api.ApiApiDelegate;
import com.customer.business.mapper.CustomerMapper;
import com.customer.business.model.CustomerRequest;
import com.customer.business.model.CustomerResponse;
import com.customer.business.model.ProductIdRequest;
import com.customer.business.model.entity.Customer;
import com.customer.business.service.CustomerService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomerApiDelegateImpl implements ApiApiDelegate {

    private final CustomerService customerService;

    public CustomerApiDelegateImpl(CustomerService customerService) {
        this.customerService = customerService;
    }

    @Override
    public ResponseEntity<CustomerResponse> createCustomer(CustomerRequest customerRequest) {
        Customer customer = CustomerMapper.getCustomerofCustomerRequest(customerRequest);

        Customer saved = customerService.create(customer);
        CustomerResponse resp = CustomerMapper.getCustomerResponseOfCustomer(saved);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }

    @Override
    public ResponseEntity<Void> deleteCustomer(String id) {
        return customerService.findById(id)
                .map(c -> {
                    customerService.delete(id);
                    return ResponseEntity.noContent().<Void>build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<List<CustomerResponse>> getAllCustomers() {
        List<Customer> list = customerService.findAll();
        List<CustomerResponse> responses = list.stream()
                .map(CustomerMapper::getCustomerResponseOfCustomer)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @Override
    public ResponseEntity<CustomerResponse> getCustomerById(String id) {
        return customerService.findById(id)
                .map(c -> ResponseEntity.ok(CustomerMapper.getCustomerResponseOfCustomer(c)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Override
    public ResponseEntity<CustomerResponse> updateCustomer(String id, CustomerRequest customerRequest) {
        Customer toUpdate = CustomerMapper.getCustomerofCustomerRequest(customerRequest);
        try {
            Customer updated = customerService.update(id, toUpdate);
            return ResponseEntity.ok(CustomerMapper.getCustomerResponseOfCustomer(updated));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<List<String>> getCustomerProducts(String id) {
        List<String> productIds = customerService.getProductIds(id);
        return ResponseEntity.ok(productIds);
    }

    @Override
    public ResponseEntity<Void> addProductToCustomer(String id, ProductIdRequest productIdRequest) {
        if (productIdRequest == null || productIdRequest.getProductId() == null || productIdRequest.getProductId().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        try {
            customerService.addProduct(id, productIdRequest.getProductId());
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<Void> removeProductFromCustomer(String id, String productId) {
        try {
            customerService.removeProduct(id, productId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
