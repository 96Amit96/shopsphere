package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventoryDeductionResultEvent;
import com.shopsphere.orderservice.service.InventoryDeductionResultProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionResultEventConsumer {

    private final InventoryDeductionResultProcessingService processingService;

    @KafkaListener(
            topics = "inventory-deduction-results",
            containerFactory =
                    "inventoryDeductionResultKafkaListenerContainerFactory"
    )
    public void consume(InventoryDeductionResultEvent event) {

        log.info(
                "Received InventoryDeductionResultEvent. eventId :: {}, " +
                        "deductionRequestEventId :: {}, orderId :: {}, successful :: {}",
                event.eventId(),
                event.deductionRequestEventId(),
                event.orderId(),
                event.successful()
        );

        processingService.process(event);
    }
}
