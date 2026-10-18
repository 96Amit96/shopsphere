package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.event.InventoryCompensationResultEvent;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryCompensationResultProcessingService {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public void process(InventoryCompensationResultEvent event) {

        if (event == null
                || event.eventId() == null
                || event.compensationRequestEventId() == null
                || event.orderId() == null
                || event.userId() == null) {
            throw new IllegalArgumentException(
                    "Invalid inventory compensation result event"
            );
        }

        // Prevent the same result event from being applied twice.
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Compensation result already processed. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );
            return;
        }

        Order order = orderRepository.findById((event.orderId()))
                .orElseThrow(() -> new IllegalStateException("Order not found for compensation result. orderId :: "+ event.orderId()));

        if (event.successful()) {

            if (order.getSagaStatus() == SagaStatus.COMPENSATING) {

                order.setOrderStatus(OrderStatus.CANCELLED);
                order.setPaymentStatus(PaymentStatus.FAILED);
                order.setSagaStatus(SagaStatus.FAILED);

                log.info(
                        "Inventory compensation confirmed. Saga marked FAILED. orderId :: {}",
                        event.orderId()
                );
            } else if (order.getSagaStatus() != SagaStatus.FAILED) {

                log.warn(
                        "Received successful compensation result, but order is not COMPENSATING. " +
                                "orderId :: {}, sagaStatus :: {}",
                        event.orderId(),
                        order.getSagaStatus()
                );
            } else {
                // Compensation did not complete. Do not mark the Saga as FAILED
                // because reserved stock may still need to be released.
                if (order.getSagaStatus() != SagaStatus.FAILED
                        && order.getSagaStatus() != SagaStatus.COMPLETED) {
                    order.setSagaStatus(SagaStatus.COMPENSATING);
                }

                log.error(
                        "Inventory compensation failed. orderId :: {}, reason :: {}",
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
    }
}

