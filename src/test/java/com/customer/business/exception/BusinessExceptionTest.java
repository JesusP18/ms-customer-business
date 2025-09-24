package com.customer.business.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BusinessExceptionTest {

    @Test
    @DisplayName("Cobertura de constructores y getters")
    void testConstructorsAndGetters() {
        BusinessException ex1 = new BusinessException(
                "msg1", HttpStatus.BAD_REQUEST, "CODE1");
        assertEquals("msg1", ex1.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex1.getStatus());
        assertEquals("CODE1", ex1.getCode());

        BusinessException ex2 = new BusinessException(
                "msg2", HttpStatus.CONFLICT);
        assertEquals("msg2", ex2.getMessage());
        assertEquals(HttpStatus.CONFLICT, ex2.getStatus());
        assertEquals("BUSINESS_ERROR", ex2.getCode());
    }
}

