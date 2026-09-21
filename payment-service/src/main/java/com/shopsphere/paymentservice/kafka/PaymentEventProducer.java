package com.shopsphere.paymentservice.kafka;

import com.shopsphere.paymentservice.dto.event.PaymentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventProducer {

    private static final String TOPIC = "payment-events";

    private final KafkaTemplate<String, PaymentEvent> kafkaTemplate;

    public void publishPaymentEvent(PaymentEvent event) {

        log.info(
                "Publishing payment event. paymentId :: {}, orderId :: {}, status :: {}",
                event.paymentId(),
                event.orderId(),
                event.paymentStatus()
        );

        kafkaTemplate.send(
                TOPIC,
                String.valueOf(event.orderId()),
                event
        );
    }
}
