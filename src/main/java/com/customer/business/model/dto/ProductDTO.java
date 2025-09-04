package com.customer.business.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductDTO {

    private String id;

    private String customerId;

    private String category; // LIABILITY | ASSET (opcional)

    private String type;     // ACCOUNT | LOAN | CREDIT_CARD

    private String subType;  // SAVINGS | CURRENT | FIXED_TERM | PERSONAL_LOAN | BUSINESS_LOAN | ...
}