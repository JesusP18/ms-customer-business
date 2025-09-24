package com.customer.business.validator;

import com.customer.business.exception.ValidationException;
import com.customer.business.model.dto.ProductDTO;
import com.customer.business.util.enums.CustomerType;
import com.customer.business.util.enums.ProductSubType;
import com.customer.business.util.enums.ProductType;
import com.customer.business.util.enums.ProfileEnum;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class AddProductValidatorServiceTest {

    @InjectMocks
    private AddProductValidatorService validatorService;

    @Test
    void validateBusinessRulesWhenBusinessCustomerWithSavingsAccountShouldThrowException() {
        List<ProductDTO> existingProducts = List.of(
                createProductDTO(ProductType.ACCOUNT, ProductSubType.SAVINGS)
        );

        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.BUSINESS.getValue(),
                        ProfileEnum.STANDARD.getValue(),
                        ProductType.ACCOUNT.getValue(),
                        ProductSubType.SAVINGS.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenBusinessCustomerWithFixedTermShouldThrowException() {
        List<ProductDTO> existingProducts = List.of(
                createProductDTO(ProductType.ACCOUNT, ProductSubType.FIXED_TERM)
        );
        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.BUSINESS.getValue(),
                        ProfileEnum.STANDARD.getValue(),
                        ProductType.ACCOUNT.getValue(),
                        ProductSubType.FIXED_TERM.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenPersonalCustomerWithTwoSavingsShouldThrowException() {
        List<ProductDTO> existingProducts = List.of(
                createProductDTO(ProductType.ACCOUNT, ProductSubType.SAVINGS)
        );
        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.PERSONAL.getValue(),
                        ProfileEnum.STANDARD.getValue(),
                        ProductType.ACCOUNT.getValue(),
                        ProductSubType.SAVINGS.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenPersonalCustomerWithTwoCurrentShouldThrowException() {
        List<ProductDTO> existingProducts = List.of(
                createProductDTO(ProductType.ACCOUNT, ProductSubType.CURRENT)
        );
        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.PERSONAL.getValue(),
                        ProfileEnum.STANDARD.getValue(),
                        ProductType.ACCOUNT.getValue(),
                        ProductSubType.CURRENT.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenPersonalCustomerWithTwoPersonalLoansShouldThrowException() {
        List<ProductDTO> existingProducts = List.of(
                createProductDTO(ProductType.LOAN, ProductSubType.PERSONAL_LOAN)
        );
        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.PERSONAL.getValue(),
                        ProfileEnum.STANDARD.getValue(),
                        ProductType.LOAN.getValue(),
                        ProductSubType.PERSONAL_LOAN.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenVipWithoutCreditCardShouldThrowException() {
        List<ProductDTO> existingProducts = List.of();
        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.PERSONAL.getValue(),
                        ProfileEnum.VIP.getValue(),
                        ProductType.ACCOUNT.getValue(),
                        ProductSubType.SAVINGS.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenPymeWithoutCreditCardShouldThrowException() {
        List<ProductDTO> existingProducts = List.of();
        assertThrows(ValidationException.class, () ->
                validatorService.validateBusinessRules(
                        CustomerType.BUSINESS.getValue(),
                        ProfileEnum.PYME.getValue(),
                        ProductType.ACCOUNT.getValue(),
                        ProductSubType.CURRENT.getValue(),
                        existingProducts
                ));
    }

    @Test
    void validateBusinessRulesWhenAllValidShouldNotThrowException() {
        ProductDTO creditCard = new ProductDTO();
        creditCard.setType(ProductType.CREDIT_CARD.getValue());
        creditCard.setSubType(ProductSubType.SAVINGS.getValue());
        List<ProductDTO> existingProducts = List.of(creditCard);
        validatorService.validateBusinessRules(
                CustomerType.PERSONAL.getValue(),
                ProfileEnum.VIP.getValue(),
                ProductType.ACCOUNT.getValue(),
                ProductSubType.SAVINGS.getValue(),
                existingProducts
        );
    }

    @Test
    void getCustomerTypeShouldReturnDefaultIfNull() {
        String result = validatorService.getCustomerType(null);
        org.junit.jupiter.api.Assertions.assertEquals(CustomerType.PERSONAL.getValue(), result);
    }

    @Test
    void getCustomerTypeShouldReturnValueIfNotNull() {
        String result = validatorService.getCustomerType("BUSINESS");
        org.junit.jupiter.api.Assertions.assertEquals("BUSINESS", result);
    }

    @Test
    void getCustomerProfileShouldReturnDefaultIfNull() {
        String result = validatorService.getCustomerProfile(null);
        org.junit.jupiter.api.Assertions.assertEquals(ProfileEnum.STANDARD.getValue(), result);
    }

    @Test
    void getCustomerProfileShouldReturnValueIfNotNull() {
        String result = validatorService.getCustomerProfile("VIP");
        org.junit.jupiter.api.Assertions.assertEquals("VIP", result);
    }

    private ProductDTO createProductDTO(ProductType type, ProductSubType subType) {
        ProductDTO dto = new ProductDTO();
        dto.setType(type.getValue());
        if (subType != null) {
            dto.setSubType(subType.getValue());
        }
        return dto;
    }
}