package com.shopsphere.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.PaymentEvent;
import com.shopsphere.notificationservice.service.PaymentNotificationService;
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

    @KafkaListener(topics = "payment-events")
    public void consumePaymentEvent(String message)
            throws JsonProcessingException {

        log.info("Received message from payment-events");

        PaymentEvent event =
                objectMapper.readValue(message, PaymentEvent.class);

        log.info(
                "Deserialized PaymentEvent. eventId={}, orderId={}, status={}",
                event.eventId(),
                event.orderId(),
                event.paymentStatus()
        );

        paymentNotificationService.notifyPaymentStatus(event);
    }

}
