package com.customer.business.model.entity;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

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

    private List<Product> products; // Lista de productos asociados al cliente
}
