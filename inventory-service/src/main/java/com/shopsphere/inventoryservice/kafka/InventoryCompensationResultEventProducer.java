package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryCompensationResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationResultEventProducer {

    private static final String TOPIC = "inventory-compensation-results";

    private final KafkaTemplate<String, InventoryCompensationResultEvent>
            inventoryCompensationResultKafkaTemplate;

    public void publishResult(InventoryCompensationResultEvent event) {

        inventoryCompensationResultKafkaTemplate.send(
                TOPIC,
                event.orderId().toString(),
                event
        ).whenComplete((result, exception) -> {

            if (exception != null) {
                log.error(
                        "Failed to publish compensation result. orderId :: {}, requestEventId :: {}",
                        event.orderId(),
                        event.compensationRequestEventId(),
                        exception
                );
                return;
            }

            log.info(
                    "Published inventory compensation result. orderId :: {}, " +
                            "successful :: {}, topic :: {}, partition :: {}, offset :: {}",
                    event.orderId(),
                    event.successful(),
                    result.getRecordMetadata().topic(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );
        });
    }
}
