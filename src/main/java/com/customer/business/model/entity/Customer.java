package com.customer.business.model.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;

    private String customerType; // PERSONAL | BUSINESS

    private String profile; // STANDARD | VIP | PYME

    private String firstName;

    private String lastName;

    private String businessName;

    private String dni;

    private String ruc;

    private String address;

    private String phone;

    private String email;
}
