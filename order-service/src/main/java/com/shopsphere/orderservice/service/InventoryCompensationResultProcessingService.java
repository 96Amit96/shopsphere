package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.InventoryCompensationResultEvent;
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
public class InventoryCompensationResultProcessingService {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final OrderOutboxService orderOutboxService;

    @Transactional
    public void process(InventoryCompensationResultEvent event) {

        validateEvent(event);

        // Prevent duplicate result processing.
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Compensation result already processed. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found for compensation result. orderId :: "
                                + event.orderId()
                ));

        if (!Objects.equals(order.getUserId(), event.userId())) {
            throw new IllegalArgumentException(
                    "User ID does not match order for compensation result"
            );
        }

        if (event.successful()) {

            if (order.getSagaStatus() == SagaStatus.COMPENSATING) {

                // Compensation completed successfully.
                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setSagaStatus(SagaStatus.FAILED);

                log.info(
                        "Inventory compensation confirmed. Saga marked FAILED. orderId :: {}",
                        event.orderId()
                );

                // Create cancellation notification event.
                OrderStatusNotificationEvent notificationEvent =
                        new OrderStatusNotificationEvent(
                                UUID.randomUUID(),
                                order.getId(),
                                order.getUserId(),
                                order.getTotalAmount(),
                                OrderStatus.CANCELLED.name(),
                                "Payment was unsuccessful and the reserved inventory has been released.",
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

                // Save notification event to transactional outbox.
                orderOutboxService.saveOrderCancelledEvent(notificationEvent);

                log.info(
                        "Order cancellation notification recorded in outbox. orderId :: {}",
                        order.getId()
                );

            } else if (order.getSagaStatus() == SagaStatus.FAILED
                    && order.getOrderStatus() == OrderStatus.CANCELLED) {

                log.info(
                        "Order is already cancelled. No additional notification required. orderId :: {}",
                        event.orderId()
                );

            } else {

                log.warn(
                        "Received successful compensation result, but order is not COMPENSATING. "
                                + "orderId :: {}, sagaStatus :: {}",
                        event.orderId(),
                        order.getSagaStatus()
                );
            }

        } else {

            // Compensation failed. Keep the order in COMPENSATING state
            // so a separate recovery/reconciliation process can retry it.
            log.error(
                    "Inventory compensation failed. orderId :: {}, reason :: {}",
                    event.orderId(),
                    event.failureReason()
            );
        }

        orderRepository.save(order);

        // Record the result event as processed.
        ProcessedEvent processedEvent = new ProcessedEvent();
        processedEvent.setEventId(event.eventId());
        processedEvent.setProcessedAt(LocalDateTime.now());

        processedEventRepository.save(processedEvent);
    }

    private void validateEvent(InventoryCompensationResultEvent event) {

        if (event == null
                || event.eventId() == null
                || event.compensationRequestEventId() == null
                || event.orderId() == null
                || event.userId() == null) {

            throw new IllegalArgumentException(
                    "Invalid inventory compensation result event"
            );
        }
    }
}