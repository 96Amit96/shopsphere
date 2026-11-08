package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.*;
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
    private final OrderOutboxService orderOutboxService;

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

            String cancellationReason =
                    event.failureReason() == null || event.failureReason().isBlank()
                            ? "Inventory reservation failed. The order could not be processed."
                            : event.failureReason();

            log.error(
                    "Inventory reservation failed for orderId :: {}, reason :: {}",
                    event.orderId(),
                    event.failureReason()
            );

            // Create order cancellation notification event.
            OrderStatusNotificationEvent notificationEvent =
                    new OrderStatusNotificationEvent(
                            UUID.randomUUID(),
                            order.getId(),
                            order.getUserId(),
                            order.getTotalAmount(),
                            OrderStatus.CANCELLED.name(),
                            cancellationReason,
                            order.getItems()
                                    .stream()
                                    .map(item -> new OrderItemEvent(
                                            item.getProductId(),
                                            item.getProductName(),
                                            item.getUnitPrice(),
                                            item.getQuantity()
                                    ))
                                    .toList()
                    );
            orderOutboxService.saveOrderCancelledEvent(notificationEvent);

            log.info(
                    "Order cancellation notification recorded in outbox. orderId :: {}",
                    order.getId()
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
