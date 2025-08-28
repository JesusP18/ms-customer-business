package com.customer.business.util.enums;

public enum ProfileEnum {
    STANDARD("STANDARD"),
    
    VIP("VIP"),
    
    PYME("PYME");

    private String value;

    ProfileEnum(String value) {
        this.value = value;
    }
}