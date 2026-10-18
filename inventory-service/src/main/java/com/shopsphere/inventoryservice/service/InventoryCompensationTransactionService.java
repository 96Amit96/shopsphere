package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.InventoryCompensationItemEvent;
import com.shopsphere.inventoryservice.dto.event.InventoryCompensationRequestedEvent;
import com.shopsphere.inventoryservice.entity.Inventory;
import com.shopsphere.inventoryservice.entity.ProcessedEvent;
import com.shopsphere.inventoryservice.exception.InvalidRequestException;
import com.shopsphere.inventoryservice.exception.ResourceNotFoundException;
import com.shopsphere.inventoryservice.repository.InventoryRepository;
import com.shopsphere.inventoryservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationTransactionService {

    private final InventoryRepository inventoryRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void releaseReservedStock(InventoryCompensationRequestedEvent event) {

        validateEvent(event);

        // Idempotency: do not release stock twice for the same request.
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Compensation request already processed. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        for (InventoryCompensationItemEvent item : event.items()) {

            Inventory inventory = inventoryRepository
                    .findByProductId(item.productId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inventory not found for product id :: "
                                    + item.productId()
                    ));

            int reservedQuantity = inventory.getReservedQuantity();

            if (item.quantity() > reservedQuantity) {
                throw new InvalidRequestException(
                        "Cannot release more stock than reserved for product id :: "
                                + item.productId()
                                + ". Reserved quantity :: "
                                + reservedQuantity
                                + ", requested release :: "
                                + item.quantity()
                );
            }

            inventory.setReservedQuantity(reservedQuantity - item.quantity());
            inventoryRepository.save(inventory);

            log.info(
                    "Inventory compensation applied. orderId :: {}, " +
                            "productId :: {}, releasedQuantity :: {}, " +
                            "remainingReservedQuantity :: {}",
                    event.orderId(),
                    item.productId(),
                    item.quantity(),
                    inventory.getReservedQuantity()
            );

            // Mark the event as processed in the same transaction.
            // Mark the event as processed in the same transaction.
            ProcessedEvent processedEvent = new ProcessedEvent();
            processedEvent.setEventId(event.eventId());
            processedEvent.setProcessedAt(LocalDateTime.now());

            processedEventRepository.save(processedEvent);

            log.info(
                    "Inventory compensation transaction completed. orderId :: {}, eventId :: {}",
                    event.orderId(),
                    event.eventId()
            );

        }
    }
    private void validateEvent(InventoryCompensationRequestedEvent event) {

        if (event == null
                || event.eventId() == null
                || event.orderId() == null
                || event.userId() == null
                || event.items() == null
                || event.items().isEmpty()) {

            throw new InvalidRequestException(
                    "Invalid inventory compensation request"
            );
        }

            for (InventoryCompensationItemEvent item : event.items()) {

                if (item == null
                        || item.productId() == null
                        || item.quantity() == null
                        || item.quantity() <= 0) {

                    throw new InvalidRequestException(
                            "Invalid item in inventory compensation request"
                    );
                }
        }
    }
}
