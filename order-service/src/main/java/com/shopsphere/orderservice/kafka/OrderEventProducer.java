package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.OrderCreatedEvent;
import com.shopsphere.orderservice.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private final OrderOutboxService orderOutboxService;

    public void publishOrderCreated(OrderCreatedEvent event) {

        orderOutboxService.saveOrderCreatedEvent(event);

        log.info(
                "OrderCreatedEvent saved to outbox. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );
    }
}
