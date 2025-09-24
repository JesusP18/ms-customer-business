package com.customer.business.event.dto;

import com.customer.business.model.entity.Customer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomerEventTest {

    @Test
    @DisplayName("Cobertura completa de constructores, getters y setters")
    void testAllMethods() {
        Customer customer = new Customer();
        LocalDateTime now = LocalDateTime.now();
        CustomerEvent event = new CustomerEvent();
        event.setEventType("CREATED");
        event.setCustomer(customer);
        event.setTimestamp(now);

        assertEquals("CREATED", event.getEventType());
        assertEquals(customer, event.getCustomer());
        assertEquals(now, event.getTimestamp());

        CustomerEvent event2 = new CustomerEvent("UPDATED", customer, now);
        assertEquals("UPDATED", event2.getEventType());
        assertEquals(customer, event2.getCustomer());
        assertEquals(now, event2.getTimestamp());
        assertNotNull(event2.toString()); // Lombok toString
    }
}

