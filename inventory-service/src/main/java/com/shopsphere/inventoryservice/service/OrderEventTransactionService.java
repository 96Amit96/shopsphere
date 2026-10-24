package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.*;
import com.shopsphere.inventoryservice.dto.request.StockReservationRequest;
import com.shopsphere.inventoryservice.entity.ProcessedEvent;
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
public class OrderEventTransactionService {

    private final ProcessedEventRepository processedEventRepository;
    private final InventoryReservationRetryService inventoryReservationRetryService;
    private final InventoryOutboxService inventoryOutboxService;

    @Transactional
    public boolean processOrderCreatedEvent(OrderCreatedEvent event) {

        if (processedEventRepository.existsById(event.eventId())) {

            log.info(
                    "Duplicate OrderCreatedEvent ignored. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );

            return false;
        }

        log.info(
                "Processing new OrderCreatedEvent. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );

        for (OrderItemEvent item : event.items()) {

            InventoryReservationRetryService inventoryReservationRetryService =
                    this.inventoryReservationRetryService;

            inventoryReservationRetryService.reserveStock(
                    item.productId(),
                    new StockReservationRequest(item.quantity())
            );
        }

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.eventId())
                .orderId(event.orderId())
                .eventType("ORDER_CREATED")
                .processedAt(LocalDateTime.now())
                .build();

        processedEventRepository.save(processedEvent);

        log.info(
                "OrderCreatedEvent processed successfully. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );

        InventorySagaEvent inventoryEvent = new InventorySagaEvent(
                UUID.randomUUID(),
                event.eventId(),
                event.orderId(),
                event.userId(),
                InventoryEventType.INVENTORY_RESERVED,
                event.items()
                        .stream()
                        .map(item ->
                                new InventoryReservationItemEvent(
                                        item.productId(),
                                        item.quantity()
                                )
                        )
                        .toList(),
                null,
                null,
                LocalDateTime.now()
        );

        log.info("Save inventory saga event :: {}", inventoryEvent.eventId());

        inventoryOutboxService.saveInventorySagaEvent(inventoryEvent);

        return true;
    }
}
