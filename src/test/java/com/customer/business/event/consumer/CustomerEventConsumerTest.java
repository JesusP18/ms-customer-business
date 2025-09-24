package com.customer.business.event.consumer;

import com.customer.business.event.dto.CustomerEvent;
import com.customer.business.model.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

class CustomerEventConsumerTest {

    @Test
    @DisplayName("consumeCustomerEvent ejecuta sin errores")
    void consumeCustomerEventShouldLogEvent() {
        CustomerEventConsumer consumer = new CustomerEventConsumer();
        Customer customer = new Customer();
        CustomerEvent event = new CustomerEvent(
            "CREATED",
            customer,
            LocalDateTime.now()
        );
        consumer.consumeCustomerEvent(event);
        // No assertions: solo se busca cobertura de ejecución
    }
}

