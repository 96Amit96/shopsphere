package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventorySagaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    private final KafkaTemplate<String, InventorySagaEvent> kafkaTemplate;

    public void publishInventoryEvent(InventorySagaEvent event) {

        kafkaTemplate.send(
                INVENTORY_EVENTS_TOPIC,
                event.orderId().toString(),
                event
        );

        log.info(
                "Published InventorySagaEvent. eventId :: {}, orderEventId :: {}, orderId :: {}, eventType :: {}",
                event.eventId(),
                event.orderEventId(),
                event.orderId(),
                event.eventType()
        );
    }
}
