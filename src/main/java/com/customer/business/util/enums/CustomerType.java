package com.customer.business.util.enums;

import lombok.Getter;

@Getter
public enum CustomerType {
    PERSONAL("PERSONAL"), BUSINESS("BUSINESS");

    private String value;

    CustomerType(String value) {
        this.value = value;
    }
}
