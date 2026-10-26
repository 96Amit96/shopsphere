package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.dto.event.OrderCreatedEvent;
import com.shopsphere.notificationservice.dto.event.OrderItemEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class OrderNotificationService {

    public void notifyOrderCreated(OrderCreatedEvent event) {
        log.info("========== ORDER NOTIFICATION ==========");
        log.info("Simulated notification: Order received successfully.");
        log.info("Event ID: {}", event.eventId());
        log.info("Order ID: {}", event.orderId());
        log.info("User ID: {}", event.userId());
        log.info("Total amount: {}", event.totalAmount());

        if (event.items() != null) {
            for (OrderItemEvent item : event.items()) {
                log.info(
                        "Item: {}, quantity: {}, unit price: {}",
                        item.productName(),
                        item.quantity(),
                        item.unitPrice()
                );
            }
        }

        log.info("========================================");
    }
}
