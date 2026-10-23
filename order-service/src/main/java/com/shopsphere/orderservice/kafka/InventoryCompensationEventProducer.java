package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventoryCompensationRequestedEvent;
import com.shopsphere.orderservice.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationEventProducer {

    private final OrderOutboxService orderOutboxService;

    public void publishCompensationRequested(InventoryCompensationRequestedEvent event) {

        orderOutboxService.saveInventoryCompensationRequestedEvent(event);

        log.info(
                "InventoryCompensationRequestedEvent recorded in outbox. "
                        + "eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );
    }
}
