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
    public void consumePaymentEvent(PaymentEvent event) {

        log.info(
                "Payment event received. paymentId :: {}, orderId :: {}, status :: {}",
                event.paymentId(),
                event.orderId(),
                event.paymentStatus()
        );

        switch (event.paymentStatus()) {

            case SUCCESS -> {
                log.info(
                        "Processing successful payment event for orderId :: {}",
                        event.orderId()
                );
                orderService.updatePaymentStatus(
                        event.orderId(),
                        event.paymentStatus()
                );
            }

            case FAILED -> {
                log.info(
                        "Processing failed payment event for orderId :: {}",
                        event.orderId()
                );

                orderService.handlePaymentFailure(event.orderId());
            }

            default -> log.info("Ignoring payment event with status :: {}",
                    event.paymentStatus());
        }
    }
}
