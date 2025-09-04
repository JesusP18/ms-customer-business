package com.customer.business.util.enums;

import lombok.Getter;

@Getter
public enum ProductType {

    ACCOUNT("ACCOUNT"),
    LOAN("LOAN"),
    CREDIT_CARD("CREDIT_CARD");

    private String value;

    ProductType(String value) {
        this.value = value;
    }
}
