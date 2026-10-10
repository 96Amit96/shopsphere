package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.InventorySagaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProcessingService {

    private final InventoryEventTransactionService inventoryEventTransactionService;

    public void processInventoryEvent(InventorySagaEvent event) {
        log.info(
                "Processing InventorySagaEvent through Saga. eventId :: {}, orderId :: {}, eventType :: {}",
                event.eventId(),
                event.orderId(),
                event.eventType()
        );
        inventoryEventTransactionService.processInventoryEvent(event);
    }
}
