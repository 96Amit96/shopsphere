package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryDeductionRequestedEvent;
import com.shopsphere.inventoryservice.dto.event.InventoryDeductionResultEvent;
import com.shopsphere.inventoryservice.exception.InvalidRequestException;
import com.shopsphere.inventoryservice.exception.ResourceNotFoundException;
import com.shopsphere.inventoryservice.service.InventoryDeductionProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionRequestedEventConsumer {

    private final InventoryDeductionProcessingService processingService;
    private final InventoryDeductionResultEventProducer resultProducer;

    @KafkaListener(
            topics = "inventory-deduction-requests",
            containerFactory = "inventoryDeductionKafkaListenerContainerFactory"
    )
    public void consume(InventoryDeductionRequestedEvent event) {

        log.info(
                "Received InventoryDeductionRequestedEvent. " +
                        "eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );

        try {
            processingService.processDeduction(event);
        } catch (InvalidRequestException | ResourceNotFoundException ex) {

            log.error(
                    "Inventory deduction failed. orderId :: {}, reason :: {}",
                    event.orderId(),
                    ex.getMessage()
            );

            publishResult(event, false, ex.getMessage());
        }
    }

    private void publishResult(
            InventoryDeductionRequestedEvent request,
            boolean successful,
            String failureReason
    ) {
        InventoryDeductionResultEvent result =
                new InventoryDeductionResultEvent(
                        UUID.randomUUID(),
                        request.eventId(),
                        request.orderId(),
                        request.userId(),
                        successful,
                        failureReason,
                        LocalDateTime.now()
                );

        resultProducer.publishResult(result);
    }
}
