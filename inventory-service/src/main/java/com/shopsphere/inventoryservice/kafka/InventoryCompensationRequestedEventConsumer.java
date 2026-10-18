package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryCompensationRequestedEvent;
import com.shopsphere.inventoryservice.dto.event.InventoryCompensationResultEvent;
import com.shopsphere.inventoryservice.exception.InvalidRequestException;
import com.shopsphere.inventoryservice.exception.ResourceNotFoundException;
import com.shopsphere.inventoryservice.service.InventoryCompensationProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationRequestedEventConsumer {

    private final InventoryCompensationProcessingService processingService;
    private final InventoryCompensationResultEventProducer resultProducer;

    @KafkaListener(
            topics = "inventory-compensation-requests",
            containerFactory =
                    "inventoryCompensationRequestKafkaListenerContainerFactory"
    )
    public void consume(InventoryCompensationRequestedEvent event) {

        log.info(
                "Received inventory compensation request. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );

        try {
            processingService.processCompensation(event);

            publishResult(event, true, null);

        } catch (InvalidRequestException | ResourceNotFoundException ex) {

            // A domain failure: the transaction has rolled back.
            log.error(
                    "Inventory compensation failed. orderId :: {}, reason :: {}",
                    event.orderId(),
                    ex.getMessage()
            );

            publishResult(event, false, ex.getMessage());
        }
    }

    private void publishResult(
            InventoryCompensationRequestedEvent request,
            boolean successful,
            String failureReason
    ) {
        InventoryCompensationResultEvent result =
                new InventoryCompensationResultEvent(
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
