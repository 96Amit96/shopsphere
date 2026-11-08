package com.shopsphere.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.OrderStatusNotificationEvent;
import com.shopsphere.notificationservice.service.ProcessedNotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderStatusNotificationEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedNotificationEventService processedNotificationEventService;

    @KafkaListener(topics = "order-status-events")
    public void consumeOrderStatusEvent(String message)
            throws JsonProcessingException {

        OrderStatusNotificationEvent event =
                objectMapper.readValue(
                        message,
                        OrderStatusNotificationEvent.class
                );

        log.info(
                "Received OrderStatusNotificationEvent. eventId={}, orderId={}, status={}",
                event.eventId(),
                event.orderId(),
                event.orderStatus()
        );

        String eventType = switch (event.orderStatus()) {
            case "CONFIRMED" -> "ORDER_CONFIRMED";
            case "CANCELLED" -> "ORDER_CANCELLED";
            default -> throw new IllegalArgumentException(
                    "Unsupported order status: " + event.orderStatus()
            );
        };

        boolean newlyQueued = processedNotificationEventService.enqueueIfAbsent(
                event.eventId(),
                eventType,
                message
        );

        if (!newlyQueued) {
            log.warn(
                    "Duplicate order status event skipped. eventId={}, orderId={}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        log.info(
                "Order status notification queued. eventId={}, orderId={}, userId={}, eventType={}",
                event.eventId(),
                event.orderId(),
                event.userId(),
                eventType
        );
    }
}
