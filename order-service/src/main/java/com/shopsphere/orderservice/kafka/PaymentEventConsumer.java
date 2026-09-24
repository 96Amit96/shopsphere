package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.PaymentEvent;
import com.shopsphere.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentEventConsumer {

    private final OrderService orderService;

    @KafkaListener(
            topics = "payment-events",
            groupId = "order-service"
    )
    public void consumePaymentEvent(
            PaymentEvent event
    ) {

        log.info(
                "Payment event received. eventId :: {}, orderId :: {}, status :: {}",
                event.eventId(),
                event.orderId(),
                event.paymentStatus()
        );

        orderService.processPaymentEvent(event);
    }
}
