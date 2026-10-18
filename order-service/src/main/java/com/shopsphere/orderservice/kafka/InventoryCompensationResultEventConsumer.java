package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventoryCompensationResultEvent;
import com.shopsphere.orderservice.service.InventoryCompensationResultProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationResultEventConsumer {

    private final InventoryCompensationResultProcessingService processingService;

    @KafkaListener(
            topics = "inventory-compensation-results",
            containerFactory =
                    "inventoryCompensationResultKafkaListenerContainerFactory"
    )
    public void consume(InventoryCompensationResultEvent event) {

        log.info(
                "Received inventory compensation result. eventId :: {}, " +
                        "requestEventId :: {}, orderId :: {}, successful :: {}",
                event.eventId(),
                event.compensationRequestEventId(),
                event.orderId(),
                event.successful()
        );

        processingService.process(event);
    }

}
