package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.PaymentRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestedEventProducer {

    private static final String PAYMENT_REQUESTS_TOPIC = "payment-requests";

    private final KafkaTemplate<String, PaymentRequestedEvent> paymentRequestedKafkaTemplate;

    public void publishPaymentRequested(PaymentRequestedEvent event) {

        paymentRequestedKafkaTemplate.send(
                PAYMENT_REQUESTS_TOPIC,
                event.orderId().toString(),
                event
        ).whenComplete((result, exception) -> {
            if (exception != null) {
                log.error("Failed to publish PaymentRequestedEvent. orderId :: {}", event.orderId(), exception);
                return;
            }
            log.info(
                    "Published PaymentRequestedEvent. eventId :: {}, inventoryEventId :: {}, orderId :: {}, topic :: {}, partition :: {}, offset :: {}",
                    event.eventId(),
                    event.inventoryEventId(),
                    event.orderId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
