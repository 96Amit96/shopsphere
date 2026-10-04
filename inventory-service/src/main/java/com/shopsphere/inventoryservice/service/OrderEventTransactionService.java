package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.OrderCreatedEvent;
import com.shopsphere.inventoryservice.dto.event.OrderItemEvent;
import com.shopsphere.inventoryservice.dto.request.StockReservationRequest;
import com.shopsphere.inventoryservice.entity.ProcessedEvent;
import com.shopsphere.inventoryservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventTransactionService {

    private final ProcessedEventRepository processedEventRepository;
    private final InventoryReservationRetryService inventoryReservationRetryService;

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

        return true;
    }
}
