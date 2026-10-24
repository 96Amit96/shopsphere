package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryDeductionResultEvent;
import com.shopsphere.inventoryservice.service.InventoryOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionResultEventProducer {

    private final InventoryOutboxService inventoryOutboxService;

    public void publishResult(InventoryDeductionResultEvent event) {

        inventoryOutboxService.saveInventoryDeductionResultEvent(event);

        log.info(
                "InventoryDeductionResultEvent recorded in outbox. "
                        + "eventId :: {}, requestEventId :: {}, "
                        + "orderId :: {}, successful :: {}",
                event.eventId(),
                event.deductionRequestEventId(),
                event.orderId(),
                event.successful()
        );
    }
}
