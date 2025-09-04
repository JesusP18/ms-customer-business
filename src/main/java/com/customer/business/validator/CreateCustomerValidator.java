package com.customer.business.validator;

import com.customer.business.exception.ValidationException;
import com.customer.business.model.CustomerCreateRequest;
import org.springframework.stereotype.Component;

@Component
public class CreateCustomerValidator {

    /**
     * Valida todas las reglas de negocio para crear un cliente
     */
    public void validate(CustomerCreateRequest request) {
        validateRequiredFields(request);
        validateFieldFormats(request);
        validateEnumValues(request);
    }

    /**
        * valida que los valores de enum no estén vacíos
     */
    private void validateEnumValues(CustomerCreateRequest request) {
        // Validar que los valores de enum no estén vacíos
        if (request.getCustomerType() != null && request.getCustomerType().toString().isEmpty()) {
            throw new ValidationException("Customer type cannot be empty");
        }

        if (request.getProfile() != null && request.getProfile().toString().isEmpty()) {
            throw new ValidationException("Profile cannot be empty");
        }
    }

    /**
     * valida que los campos obligatorios no sean nulos
     */
    private void validateRequiredFields(CustomerCreateRequest request) {
        customerTypeRequiredFieldValidate(request);

        profileRequiredFieldValidate(request);

        firstNameRequiredFieldValidate(request);

        lastNameRequiredFieldValidate(request);

        dniRequiredFieldValidate(request);

        phoneRequiredFieldValidate(request);

        emailRequiredFieldValidate(request);
    }

    private static void customerTypeRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getCustomerType() == null) {
            throw new ValidationException("Customer type is required");
        }
    }

    private static void profileRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getProfile() == null) {
            throw new ValidationException("Profile is required");
        }
    }

    private static void firstNameRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getFirstName() == null) {
            throw new ValidationException("First name is required");
        }
    }

    private static void lastNameRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getLastName() == null) {
            throw new ValidationException("Last name is required");
        }
    }

    private static void dniRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getDni() == null) {
            throw new ValidationException("DNI is required");
        }
    }

    private static void phoneRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getPhone() == null) {
            throw new ValidationException("Phone is required");
        }
    }

    private static void emailRequiredFieldValidate(CustomerCreateRequest request) {
        if (request.getEmail() == null) {
            throw new ValidationException("Email is required");
        }
    }

    /**
     * valida formatos de campos
     */
    private void validateFieldFormats(CustomerCreateRequest request) {
        firstNameEmptyValidate(request);

        lastNameEmptyValidate(request);

        businessEmptyValidate(request);

        dniFormatValidate(request);

        rucFormatValidate(request);

        addressEmptyValidate(request);

        phoneEmptyValidate(request);

        emailFormatValidate(request);
    }

    private static void firstNameEmptyValidate(CustomerCreateRequest request) {
        if (request.getFirstName() != null && request.getFirstName().trim().isEmpty()) {
            throw new ValidationException("First name cannot be empty");
        }
    }

    private static void lastNameEmptyValidate(CustomerCreateRequest request) {
        if (request.getLastName() != null && request.getLastName().trim().isEmpty()) {
            throw new ValidationException("Last name cannot be empty");
        }
    }

    private static void businessEmptyValidate(CustomerCreateRequest request) {
        if (request.getBusinessName() != null &&
                request.getBusinessName().trim().isEmpty()) {
            throw new ValidationException("Business name cannot be empty");
        }
    }

    private void dniFormatValidate(CustomerCreateRequest request) {
        if (request.getDni() != null && !isValidDni(request.getDni())) {
            throw new ValidationException("Invalid DNI format");
        }
    }

    private void rucFormatValidate(CustomerCreateRequest request) {
        if (request.getRuc() != null &&
                !request.getRuc().trim().isEmpty() &&
                !isValidRuc(request.getRuc())) {
            throw new ValidationException("Invalid RUC format");
        }
    }

    private static void addressEmptyValidate(CustomerCreateRequest request) {
        if (request.getAddress() != null && request.getAddress().trim().isEmpty()) {
            throw new ValidationException("Address cannot be empty");
        }
    }

    private static void phoneEmptyValidate(CustomerCreateRequest request) {
        if (request.getPhone() != null && request.getPhone().trim().isEmpty()) {
            throw new ValidationException("Phone cannot be empty");
        }
    }

    private void emailFormatValidate(CustomerCreateRequest request) {
        if (request.getEmail() != null && !isValidEmail(request.getEmail())) {
            throw new ValidationException("Invalid email format");
        }
    }

    private boolean isValidDni(String dni) {
        // Validación básica de DNI (8 dígitos)
        return dni != null && dni.matches("^[0-9]{8}$");
    }

    private boolean isValidRuc(String ruc) {
        // Validación básica de RUC (11 dígitos)
        return ruc != null && ruc.matches("^[0-9]{11}$");
    }

    private boolean isValidEmail(String email) {
        // Implementación básica de validación de email
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}