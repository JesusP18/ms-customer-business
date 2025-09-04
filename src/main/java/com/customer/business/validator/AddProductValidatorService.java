package com.customer.business.validator;

import com.customer.business.exception.ValidationException;
import com.customer.business.model.dto.ProductDTO;
import com.customer.business.util.enums.CustomerType;
import com.customer.business.util.enums.ProductSubType;
import com.customer.business.util.enums.ProductType;
import com.customer.business.util.enums.ProfileEnum;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddProductValidatorService {

    /**
     * Valida todas las reglas de negocio para agregar un producto
     */
    public void validateBusinessRules(String customerType, String customerProfile,
                                      String productType, String productSubType,
                                      List<ProductDTO> existingProducts) {
        // Contar tipos de productos existentes
        long savingsCount = countProductsByType(
                existingProducts, ProductType.ACCOUNT, ProductSubType.SAVINGS);
        long currentCount = countProductsByType(
                existingProducts, ProductType.ACCOUNT, ProductSubType.CURRENT);
        long fixedCount = countProductsByType(
                existingProducts, ProductType.ACCOUNT, ProductSubType.FIXED_TERM);
        long personalLoanCount = countProductsByType(
                existingProducts, ProductType.LOAN, ProductSubType.PERSONAL_LOAN);
        boolean hasCreditCard = hasProductType(
                existingProducts, ProductType.CREDIT_CARD);

        // Validar reglas de negocio
        validateBusinessCustomerRules(
                customerType, productType, productSubType);
        validatePersonalCustomerRules(
                customerType, productType, productSubType, savingsCount, currentCount);
        validatePersonalLoanRules(
                customerType, productType, productSubType, personalLoanCount);
        validateVipRules(
                customerProfile, productType, productSubType, hasCreditCard);
        validatePymeRules(
                customerType, customerProfile, productType, productSubType, hasCreditCard);
    }

    /**
     * Cuenta productos por tipo y subtipo
     */
    private long countProductsByType(
            List<ProductDTO> products, ProductType type, ProductSubType subType) {
        return products.stream()
                .filter(
                        product ->
                                type.getValue().equalsIgnoreCase(product.getType()) &&
                        subType.getValue().equalsIgnoreCase(product.getSubType()))
                .count();
    }

    /**
     * Verifica si el cliente tiene un tipo específico de producto
     */
    private boolean hasProductType(
            List<ProductDTO> products, ProductType type) {
        return products.stream()
                .anyMatch(
                        product -> type.getValue().equalsIgnoreCase(product.getType()));
    }

    /**
     * Valida reglas para clientes empresariales
     */
    private void validateBusinessCustomerRules(
            String customerType, String productType, String productSubType) {
        if (CustomerType.BUSINESS.getValue().equalsIgnoreCase(customerType) &&
                ProductType.ACCOUNT.getValue().equalsIgnoreCase(productType) &&
                (ProductSubType.SAVINGS.getValue().equalsIgnoreCase(productSubType) ||
                        ProductSubType.FIXED_TERM.getValue().equalsIgnoreCase(productSubType))) {
            throw new ValidationException(
                    "Business customers cannot have savings or fixed-term accounts"
            );
        }
    }

    /**
     * Valida reglas para clientes personales
     */
    private void validatePersonalCustomerRules(
            String customerType, String productType,
            String productSubType, long savingsCount, long currentCount) {
        if (CustomerType.PERSONAL.getValue().equalsIgnoreCase(customerType) &&
                ProductType.ACCOUNT.getValue().equalsIgnoreCase(productType)) {

            if (ProductSubType.SAVINGS
                    .getValue()
                    .equalsIgnoreCase(productSubType) &&
                    savingsCount >= 1) {
                throw new ValidationException(
                        "Personal customer already has a savings account"
                );
            }

            if (ProductSubType.CURRENT
                    .getValue()
                    .equalsIgnoreCase(productSubType) &&
                    currentCount >= 1) {
                throw new ValidationException(
                        "Personal customer already has a current account"
                );
            }
        }
    }

    /**
     * Valida reglas de préstamos personales
     */
    private void validatePersonalLoanRules(String customerType, String productType,
                                           String productSubType, long personalLoanCount) {
        if (ProductType.LOAN.getValue().equalsIgnoreCase(productType) &&
                ProductSubType.PERSONAL_LOAN.getValue().equalsIgnoreCase(productSubType) &&
                CustomerType.PERSONAL.getValue().equalsIgnoreCase(customerType) &&
                personalLoanCount >= 1) {
            throw new ValidationException("Personal customer already has a personal loan");
        }
    }

    /**
     * Valida reglas VIP
     */
    private void validateVipRules(String customerProfile, String productType,
                                  String productSubType, boolean hasCreditCard) {
        if (ProfileEnum.VIP.getValue().equalsIgnoreCase(customerProfile) &&
                ProductType.ACCOUNT.getValue().equalsIgnoreCase(productType) &&
                ProductSubType.SAVINGS.getValue().equalsIgnoreCase(productSubType) &&
                !hasCreditCard) {
            throw new ValidationException(
                    "VIP personal must have a credit card to create a VIP savings account"
            );
        }
    }

    /**
     * Valida reglas PYME
     */
    private void validatePymeRules(String customerType, String customerProfile,
                                   String productType, String productSubType,
                                   boolean hasCreditCard) {
        if (ProfileEnum.PYME.getValue().equalsIgnoreCase(customerProfile) &&
                CustomerType.BUSINESS.getValue().equalsIgnoreCase(customerType) &&
                ProductType.ACCOUNT.getValue().equalsIgnoreCase(productType) &&
                ProductSubType.CURRENT.getValue().equalsIgnoreCase(productSubType) &&
                !hasCreditCard) {
            throw new ValidationException(
                    "PYME must have a credit card to create the PYME current account"
            );
        }
    }

    /**
     * Método auxiliar para obtener el tipo de cliente con valor por defecto
     */
    public String getCustomerType(String customerTypeFromEntity) {
        return customerTypeFromEntity != null ?
                customerTypeFromEntity :
                CustomerType.PERSONAL.getValue();
    }

    /**
     * Método auxiliar para obtener el perfil del cliente con valor por defecto
     */
    public String getCustomerProfile(
            String customerProfileFromEntity) {
        return customerProfileFromEntity != null ?
                customerProfileFromEntity :
                ProfileEnum.STANDARD.getValue();
    }
}