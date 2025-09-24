package com.customer.business.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ResourceNotFoundExceptionTest {

    @Test
    @DisplayName("Debe crear la excepción con mensaje, status y código correctos")
    void testConstructor() {
        ResourceNotFoundException ex = new ResourceNotFoundException("Customer", "123");
        assertEquals("Customer with id 123 not found", ex.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatus());
        assertEquals("RESOURCE_NOT_FOUND", ex.getCode());
    }
}

