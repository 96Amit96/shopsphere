package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.PaymentRequestedEvent;
import com.shopsphere.orderservice.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentRequestedEventProducer {

    private final OrderOutboxService orderOutboxService;

    public void publishPaymentRequested(PaymentRequestedEvent event) {

        orderOutboxService.savePaymentRequestedEvent(event);

        log.info(
                "PaymentRequestedEvent recorded in outbox. " +
                        "eventId :: {}, inventoryEventId :: {}, orderId :: {}",
                event.eventId(),
                event.inventoryEventId(),
                event.orderId()
        );
    }
}
