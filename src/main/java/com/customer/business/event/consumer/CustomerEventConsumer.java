package com.customer.business.event.consumer;

import com.customer.business.event.dto.CustomerEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomerEventConsumer {
    
    @KafkaListener(topics = "customer-events")
    public void consumeCustomerEvent(CustomerEvent event) {
        log.info("Received customer event: {}", event);
        // Handle event (e.g., update read model, send notifications)
    }
}