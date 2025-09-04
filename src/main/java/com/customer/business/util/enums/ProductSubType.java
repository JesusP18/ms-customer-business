package com.customer.business.util.enums;

import lombok.Getter;

@Getter
public enum ProductSubType {

    // ACCOUNT subTypes
    SAVINGS("SAVINGS"),
    CURRENT("CURRENT"),
    FIXED_TERM("FIXED_TERM"),
    SALARY("SALARY"),
    FOREIGN_CURRENCY("FOREIGN_CURRENCY"),

    // LOAN subTypes
    PERSONAL_LOAN("PERSONAL_LOAN"),
    BUSINESS_LOAN("BUSINESS_LOAN"),
    MORTGAGE("MORTGAGE"),
    AUTO_LOAN("AUTO_LOAN"),

    // CREDIT_CARD subTypes
    STANDARD_CARD("STANDARD_CARD"),
    GOLD_CARD("GOLD_CARD"),
    PLATINUM_CARD("PLATINUM_CARD"),
    BUSINESS_CARD("BUSINESS_CARD");

    private String value;

    ProductSubType(String value) {
        this.value = value;
    }

}
