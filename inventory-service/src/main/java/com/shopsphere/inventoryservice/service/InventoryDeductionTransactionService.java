package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.InventoryDeductionItemEvent;
import com.shopsphere.inventoryservice.dto.event.InventoryDeductionRequestedEvent;
import com.shopsphere.inventoryservice.dto.event.InventoryDeductionResultEvent;
import com.shopsphere.inventoryservice.entity.Inventory;
import com.shopsphere.inventoryservice.entity.ProcessedEvent;
import com.shopsphere.inventoryservice.exception.InvalidRequestException;
import com.shopsphere.inventoryservice.exception.ResourceNotFoundException;
import com.shopsphere.inventoryservice.kafka.InventoryDeductionResultEventProducer;
import com.shopsphere.inventoryservice.repository.InventoryRepository;
import com.shopsphere.inventoryservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionTransactionService {

    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryDeductionResultEventProducer resultProducer;

    @Transactional
    public void deductReservedStock(InventoryDeductionRequestedEvent event) {

        validateEvent(event);

        // Idempotency: don't deduct stock twice for the same event.
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Inventory deduction event already processed. "
                            + "eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        // Deduct every item within the current transaction.
        for (InventoryDeductionItemEvent item : event.items()) {

            Inventory inventory = inventoryRepository
                    .findByProductId(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product id :: "
                                    + item.productId()
                    ));

            int quantity = inventory.getQuantity();
            int reservedQuantity = inventory.getReservedQuantity();

            if (item.quantity() > reservedQuantity) {
                throw new InvalidRequestException(
                        "Cannot deduct more stock than reserved for product id :: "
                                + item.productId()
                                + ". Reserved quantity :: "
                                + reservedQuantity
                );
            }

            if (item.quantity() > quantity) {
                throw new InvalidRequestException(
                        "Cannot deduct more stock than total quantity for product id :: "
                                + item.productId()
                );
            }

            // Finalize the reservation by reducing both counts equally.
            inventory.setQuantity(quantity - item.quantity());
            inventory.setReservedQuantity(
                    reservedQuantity - item.quantity()
            );

            inventoryRepository.save(inventory);

            log.info(
                    "Inventory deduction applied. orderId :: {}, "
                            + "productId :: {}, deductedQuantity :: {}, "
                            + "quantityRemaining :: {}, reservedQuantityRemaining :: {}",
                    event.orderId(),
                    item.productId(),
                    item.quantity(),
                    inventory.getQuantity(),
                    inventory.getReservedQuantity()
            );
        }

        // Record the request after all item deductions succeed.
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.eventId());
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);

        // Create the successful result event.
        InventoryDeductionResultEvent resultEvent =
                new InventoryDeductionResultEvent(
                        UUID.randomUUID(),
                        event.eventId(),
                        event.orderId(),
                        event.userId(),
                        true,
                        null,
                        LocalDateTime.now()
                );

        // Save the result to the Outbox within this transaction.
        resultProducer.publishResult(resultEvent);

        log.info(
                "Inventory deduction and result outbox record saved "
                        + "transactionally. orderId :: {}, eventId :: {}",
                event.orderId(),
                event.eventId()
        );
    }

    private void validateEvent(InventoryDeductionRequestedEvent event) {

        if (event == null
                || event.eventId() == null
                || event.paymentEventId() == null
                || event.orderId() == null
                || event.userId() == null
                || event.items() == null
                || event.items().isEmpty()) {

            throw new InvalidRequestException(
                    "Invalid inventory deduction request"
            );
        }

        for (InventoryDeductionItemEvent item : event.items()) {

            if (item == null
                    || item.productId() == null
                    || item.quantity() == null
                    || item.quantity() <= 0) {

                throw new InvalidRequestException(
                        "Invalid item in inventory deduction request"
                );
            }
        }
    }
}
