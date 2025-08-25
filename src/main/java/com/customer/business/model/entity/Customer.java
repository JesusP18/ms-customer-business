package com.customer.business.model.entity;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Document(collection = "customers")
public class Customer {

    @Id
    private String id;
    private String customerType;
    private String firstName;
    private String lastName;
    private String businessName;
    private String dni;
    private String ruc;
    private String address;
    private String phone;
    private String email;

    private List<Product> products; // Lista de productos asociados al cliente
}
