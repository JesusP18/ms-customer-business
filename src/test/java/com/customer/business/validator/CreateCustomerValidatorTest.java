package com.customer.business.validator;

import com.customer.business.exception.ValidationException;
import com.customer.business.model.CustomerCreateRequest;
import com.customer.business.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class CreateCustomerValidatorTest {

    @Mock
    private CustomerRepository customerRepository;

    @Test
    void validateWhenRequiredFieldsMissingShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenInvalidEmailFormatShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("invalid-email");

        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyFirstNameShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyLastNameShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyBusinessNameShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setBusinessName("");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenInvalidDniFormatShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("1234");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenInvalidRucFormatShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setRuc("1234");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyAddressShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        request.setAddress("");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyPhoneShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setPhone("");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenEmptyEnumShouldThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(null);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        assertThrows(ValidationException.class, () -> validator.validate(request));
    }

    @Test
    void validateWhenAllValidShouldNotThrowException() {
        CreateCustomerValidator validator = new CreateCustomerValidator(customerRepository);
        CustomerCreateRequest request = new CustomerCreateRequest();
        request.setCustomerType(CustomerCreateRequest.CustomerTypeEnum.PERSONAL);
        request.setProfile(CustomerCreateRequest.ProfileEnum.STANDARD);
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setDni("12345678");
        request.setPhone("123456789");
        request.setEmail("john@example.com");
        validator.validate(request);
    }
}