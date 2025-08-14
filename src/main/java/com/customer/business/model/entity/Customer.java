package com.customer.business.model.entity;

import lombok.*;
import org.bson.codecs.pojo.annotations.BsonId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "customers")
public class Customer {

    @BsonId
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

    private List<String> products; // Lista de IDs de productos asociados al cliente
}
