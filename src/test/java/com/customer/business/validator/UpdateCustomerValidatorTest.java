package com.customer.business.validator;

import com.customer.business.exception.ValidationException;
import com.customer.business.model.CustomerUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class UpdateCustomerValidatorTest {

    private final UpdateCustomerValidator validator = new UpdateCustomerValidator();

    @Test
    void validateWhenEmptyFirstNameShouldThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("");

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyLastNameShouldThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyBusinessNameShouldThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setBusinessName("");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyAddressShouldThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setAddress("");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyPhoneShouldThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setPhone("");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenInvalidEmailFormatShouldThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setEmail("invalid-email");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenAllValidFieldsShouldNotThrowException() {
        CustomerUpdateRequest request = new CustomerUpdateRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setBusinessName("Business");
        request.setAddress("Address");
        request.setPhone("123456789");
        request.setEmail("john@example.com");

        assertDoesNotThrow(() -> validator.validate(request));
    }
}