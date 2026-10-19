package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryDeductionResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionResultEventProducer {

    private static final String TOPIC = "inventory-deduction-results";

    private final KafkaTemplate<String, InventoryDeductionResultEvent>
            inventoryDeductionResultKafkaTemplate;

    public void publishResult(InventoryDeductionResultEvent event) {

        inventoryDeductionResultKafkaTemplate.send(
                TOPIC,
                event.orderId().toString(),
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Failed to publish inventory deduction result. " +
                                "orderId :: {}, requestEventId :: {}",
                        event.orderId(),
                        event.deductionRequestEventId(),
                        exception
                );
                return;
            }

            log.info(
                    "Published InventoryDeductionResultEvent. " +
                            "orderId :: {}, successful :: {}, topic :: {}, " +
                            "partition :: {}, offset :: {}",
                    event.orderId(),
                    event.successful(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
