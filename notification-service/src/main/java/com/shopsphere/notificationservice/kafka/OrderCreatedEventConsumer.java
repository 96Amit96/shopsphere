package com.shopsphere.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.OrderCreatedEvent;
import com.shopsphere.notificationservice.service.OrderNotificationService;
import com.shopsphere.notificationservice.service.ProcessedNotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final ObjectMapper objectMapper;
    private final OrderNotificationService orderNotificationService;
    private final ProcessedNotificationEventService processedNotificationEventService;

    @KafkaListener(topics = "order-events")
    public void consumeOrderCreatedEvent(String message)
            throws JsonProcessingException {

        OrderCreatedEvent event =
                objectMapper.readValue(message, OrderCreatedEvent.class);

        log.info(
                "Received OrderCreatedEvent. eventId={}, orderId={}",
                event.eventId(),
                event.orderId()
        );

        boolean firstProcessing = processedNotificationEventService.markAsProcessed(event.eventId(), "ORDER_CREATED");

        if (!firstProcessing) {
            log.warn(
                    "Duplicate order event skipped. eventId={}, orderId={}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        orderNotificationService.notifyOrderCreated(event);
    }
}
