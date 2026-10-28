package com.shopsphere.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.PaymentEvent;
import com.shopsphere.notificationservice.service.PaymentNotificationService;
import com.shopsphere.notificationservice.service.ProcessedNotificationEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentNotificationService paymentNotificationService;
    private final ProcessedNotificationEventService processedEventService;


    @KafkaListener(topics = "payment-events")
    public void consumePaymentEvent(String message)
            throws JsonProcessingException {

        PaymentEvent event =
                objectMapper.readValue(message, PaymentEvent.class);

        log.info(
                "Received  PaymentEvent. eventId={}, orderId={}, status={}",
                event.eventId(),
                event.orderId(),
                event.paymentStatus()
        );

        boolean newlyQueued = processedEventService.enqueueIfAbsent(
                event.eventId(),
                "PAYMENT_EVENT",
                message
        );


        if (!newlyQueued) {
            log.warn(
                    "Duplicate payment event skipped. eventId={}, orderId={}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        log.info(
                "Payment notification queued. eventId={}, orderId={}, status={}",
                event.eventId(),
                event.orderId(),
                event.paymentStatus()
        );
    }

}
