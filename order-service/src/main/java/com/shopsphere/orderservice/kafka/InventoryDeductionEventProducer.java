package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventoryDeductionRequestedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionEventProducer {

    private static final String TOPIC = "inventory-deduction-requests";

    private final KafkaTemplate<String, InventoryDeductionRequestedEvent>
            inventoryDeductionKafkaTemplate;

    public void publishDeductionRequested(InventoryDeductionRequestedEvent event) {
        inventoryDeductionKafkaTemplate.send(
                TOPIC,
                event.orderId().toString(),
                event
        ).whenComplete((result,exception) -> {
            if (exception != null) {
                log.error(
                        "Failed to publish InventoryDeductionRequestedEvent. " +
                                "eventId :: {}, orderId :: {}",
                        event.eventId(),
                        event.orderId(),
                        exception
                );
                return;
            }
            log.info(
                    "Published InventoryDeductionRequestedEvent. " +
                            "eventId :: {}, paymentEventId :: {}, orderId :: {}, " +
                            "topic :: {}, partition :: {}, offset :: {}",
                    event.eventId(),
                    event.paymentEventId(),
                    event.orderId(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
