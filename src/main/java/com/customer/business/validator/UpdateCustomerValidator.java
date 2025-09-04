package com.customer.business.validator;

import com.customer.business.exception.ValidationException;
import com.customer.business.model.CustomerUpdateRequest;
import org.springframework.stereotype.Component;

@Component
public class UpdateCustomerValidator {

    public void validate(CustomerUpdateRequest request) {
        firstNameEmptyValidate(request);

        lastNameEmptyValidate(request);

        businessEmptyValidate(request);

        addressEmptyValidate(request);

        phoneEmptyValidate(request);

        emailFormatValidate(request);
    }

    private static void firstNameEmptyValidate(CustomerUpdateRequest request) {
        if (request.getFirstName() != null && request.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name cannot be empty");
        }
    }

    private static void lastNameEmptyValidate(CustomerUpdateRequest request) {
        if (request.getLastName() != null && request.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name cannot be empty");
        }
    }

    private static void businessEmptyValidate(CustomerUpdateRequest request) {
        if (request.getBusinessName() != null && request.getBusinessName().trim().isEmpty()) {
            throw new ValidationException("Business name cannot be empty");
        }
    }

    private static void addressEmptyValidate(CustomerUpdateRequest request) {
        if (request.getAddress() != null && request.getAddress().trim().isEmpty()) {
            throw new ValidationException("Address cannot be empty");
        }
    }

    private static void phoneEmptyValidate(CustomerUpdateRequest request) {
        if (request.getPhone() != null && request.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone cannot be empty");
        }
    }

    private void emailFormatValidate(CustomerUpdateRequest request) {
        if (request.getEmail() != null && !isValidEmail(request.getEmail())) {
            throw new ValidationException("Invalid email format");
        }
    }

    private boolean isValidEmail(String email) {
        // Implementación básica de validación de email
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}