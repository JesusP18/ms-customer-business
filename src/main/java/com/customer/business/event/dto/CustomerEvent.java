package com.customer.business.event.dto;

import com.customer.business.model.entity.Customer;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CustomerEvent {

    private String eventType;

    private Customer customer;

    private LocalDateTime timestamp;

}