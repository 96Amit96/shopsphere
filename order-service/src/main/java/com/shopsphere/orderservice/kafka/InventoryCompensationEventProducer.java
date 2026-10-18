package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventoryCompensationRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationEventProducer {

    private static final String TOPIC = "inventory-compensation-requests";

    private final KafkaTemplate<String, InventoryCompensationRequestedEvent> inventoryCompensationKafkaTemplate;

    public void publishCompensationRequested(InventoryCompensationRequestedEvent event) {
        inventoryCompensationKafkaTemplate.send(
                TOPIC,
                event.orderId().toString(),
                event
        ).whenComplete((result , exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish inventory compensation request. " +
                                "eventId :: {}, orderId :: {}",
                        event.eventId(),
                        event.orderId(),
                        exception
                );
                return;
            }
            log.info(
                    "Published InventoryCompensationRequestedEvent. " +
                            "eventId :: {}, orderId :: {}, topic :: {}, " +
                            "partition :: {}, offset :: {}",
                    event.eventId(),
                    event.orderId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
