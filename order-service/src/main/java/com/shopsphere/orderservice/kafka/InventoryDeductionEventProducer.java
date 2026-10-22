package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventoryDeductionRequestedEvent;
import com.shopsphere.orderservice.service.OrderOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionEventProducer {

    private final OrderOutboxService orderOutboxService;

    public void publishDeductionRequested(
            InventoryDeductionRequestedEvent event) {

        orderOutboxService.saveInventoryDeductionRequestedEvent(event);

        log.info(
                "InventoryDeductionRequestedEvent recorded in outbox. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );
    }
}
