package com.customer.business.model.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Product {

    private String id;

    private String category; // LIABILITY | ASSET (opcional)

    private String type;     // ACCOUNT | LOAN | CREDIT_CARD

    private String subType;  // SAVINGS | CURRENT | FIXED_TERM | PERSONAL_LOAN | BUSINESS_LOAN | ...
}
