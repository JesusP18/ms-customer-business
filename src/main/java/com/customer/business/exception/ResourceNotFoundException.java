package com.customer.business.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String resourceName, String identifier) {
        super(String.format("%s with id %s not found", resourceName, identifier), 
              HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND");
    }
}