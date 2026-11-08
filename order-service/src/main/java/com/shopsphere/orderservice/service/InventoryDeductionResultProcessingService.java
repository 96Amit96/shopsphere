package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.InventoryDeductionResultEvent;
import com.shopsphere.orderservice.dto.event.OrderItemEvent;
import com.shopsphere.orderservice.dto.event.OrderStatusNotificationEvent;
import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.entity.ProcessedEvent;
import com.shopsphere.orderservice.enums.OrderStatus;
import com.shopsphere.orderservice.enums.PaymentStatus;
import com.shopsphere.orderservice.enums.SagaStatus;
import com.shopsphere.orderservice.repository.OrderRepository;
import com.shopsphere.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryDeductionResultProcessingService {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OrderOutboxService orderOutboxService;

    @Transactional
    public void process(InventoryDeductionResultEvent event) {

        validateEvent(event);

        // Prevent duplicate result processing.
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Inventory deduction result already processed. eventId :: {}",
                    event.eventId()
            );
            return;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for inventory deduction result. orderId :: "
                                + event.orderId()
                ));

        if (!Objects.equals(order.getUserId(), event.userId())) {
            throw new IllegalArgumentException(
                    "User ID does not match order for inventory deduction result"
            );
        }

        if (event.successful()) {

            if (order.getPaymentStatus() != PaymentStatus.SUCCESS) {
                throw new IllegalStateException(
                        "Cannot complete order before payment succeeds. orderId :: "
                                + event.orderId()
                );
            }

            if (order.getSagaStatus() == SagaStatus.COMPLETED) {
                log.info(
                        "Order Saga already completed. orderId :: {}",
                        event.orderId()
                );
            } else {
                order.setOrderStatus(OrderStatus.CONFIRMED);
                order.setSagaStatus(SagaStatus.COMPLETED);

                log.info(
                        "Inventory deduction confirmed. Order Saga completed. orderId :: {}",
                        event.orderId()
                );

                // Create order confirmation notification event.
                OrderStatusNotificationEvent notificationEvent =
                        new OrderStatusNotificationEvent(
                                UUID.randomUUID(),
                                order.getId(),
                                order.getUserId(),
                                order.getTotalAmount(),
                                OrderStatus.CONFIRMED.name(),
                                null,
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
                orderOutboxService.saveOrderConfirmedEvent(notificationEvent);

                log.info(
                        "Order confirmation notification recorded in outbox. orderId :: {}",
                        order.getId()
                );
            }

        } else {

            // Payment succeeded but inventory deduction failed.
            // Do not confirm the order or mark the Saga completed.
            // A retry/reconciliation or payment-refund workflow is needed.
            log.error(
                    "Inventory deduction failed after successful payment. " +
                            "orderId :: {}, reason :: {}. Order requires recovery.",
                    event.orderId(),
                    event.failureReason()
            );
        }

        orderRepository.save(order);

        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.eventId());
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);
    }

    private void validateEvent(InventoryDeductionResultEvent event) {

        if (event == null
                || event.eventId() == null
                || event.deductionRequestEventId() == null
                || event.orderId() == null
                || event.userId() == null) {
            throw new IllegalArgumentException(
                    "Invalid inventory deduction result event"
            );
        }
    }
}
