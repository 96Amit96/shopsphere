package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.InventoryEventType;
import com.shopsphere.orderservice.dto.event.InventorySagaEvent;
import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.entity.ProcessedEvent;
import com.shopsphere.orderservice.enums.OrderStatus;
import com.shopsphere.orderservice.enums.SagaStatus;
import com.shopsphere.orderservice.exception.OrderNotFoundException;
import com.shopsphere.orderservice.repository.OrderRepository;
import com.shopsphere.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventTransactionService {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void processInventoryEvent(InventorySagaEvent event) {

        if (processedEventRepository.existsById(event.eventId())) {

            log.info(
                    "Duplicate InventorySagaEvent ignored. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );

            return;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(
                        () -> new OrderNotFoundException("Order not found :: " + event.orderId())
                );

        if(event.eventType() == InventoryEventType.INVENTORY_RESERVED) {

            order.setSagaStatus(SagaStatus.INVENTORY_RESERVED);

            log.info(
                    "Inventory reserved successfully for orderId :: {}",
                    event.orderId()
            );
            
        } else if (event.eventType() == InventoryEventType.INVENTORY_RESERVATION_FAILED) {

            order.setOrderStatus(OrderStatus.CANCELLED);

            order.setSagaStatus(SagaStatus.FAILED);

            log.error(
                    "Inventory reservation failed for orderId :: {}, reason :: {}",
                    event.orderId(),
                    event.failureReason()
            );
        }

        orderRepository.save(order);

        ProcessedEvent processedEvent = new ProcessedEvent();

        processedEvent.setEventId(event.eventId());
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);

        log.info(
                "InventorySagaEvent processed successfully. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );

    }
}
