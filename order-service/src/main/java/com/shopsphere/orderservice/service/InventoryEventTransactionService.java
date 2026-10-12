package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.InventoryEventType;
import com.shopsphere.orderservice.dto.event.InventorySagaEvent;
import com.shopsphere.orderservice.dto.event.PaymentRequestedEvent;
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
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryEventTransactionService {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public PaymentRequestedEvent processInventoryEvent(InventorySagaEvent event) {

        if (processedEventRepository.existsById(event.eventId())) {

            log.info(
                    "Duplicate InventorySagaEvent ignored. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );

            return null;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(
                        () -> new OrderNotFoundException("Order not found :: " + event.orderId())
                );

        PaymentRequestedEvent paymentRequestedEvent = null;

        if(event.eventType() == InventoryEventType.INVENTORY_RESERVED) {

            if (order.getPaymentMethod() == null || order.getPaymentMethod().isBlank()) {
                throw new IllegalStateException("Payment method is missing for order :: "+ order.getId());
            }

            order.setSagaStatus(SagaStatus.PAYMENT_PENDING);

            paymentRequestedEvent = new PaymentRequestedEvent(
                    UUID.randomUUID(),
                    event.eventId(),
                    order.getId(),
                    order.getUserId(),
                    order.getTotalAmount(),
                    order.getPaymentMethod(),
                    LocalDateTime.now()
            );

            log.info(
                    "Inventory reserved successfully. Payment requested for orderId  :: {}",
                    order.getId()
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

        return paymentRequestedEvent;
    }
}
