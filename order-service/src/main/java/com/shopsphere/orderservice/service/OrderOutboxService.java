package com.shopsphere.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.orderservice.dto.event.*;
import com.shopsphere.orderservice.entity.OrderOutboxEvent;
import com.shopsphere.orderservice.enums.OrderOutboxStatus;
import com.shopsphere.orderservice.repository.OrderOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxService {

    private static final String ORDER_EVENTS_TOPIC = "order-events";
    private static final String ORDER_STATUS_EVENTS_TOPIC = "order-status-events";

    private final OrderOutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveOrderCreatedEvent(OrderCreatedEvent event) {

        log.info("Outbox OrderCreatedEvent :: {}", event);

        try {
            OrderOutboxEvent outboxEvent = new OrderOutboxEvent();

            outboxEvent.setId(event.eventId());
            outboxEvent.setEventType("ORDER_CREATED");
            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(event.orderId());
            outboxEvent.setTopic(ORDER_EVENTS_TOPIC);
            outboxEvent.setMessageKey(event.orderId().toString());
            outboxEvent.setPayload(
                    objectMapper.writeValueAsString(event)
            );
            outboxEvent.setStatus(OrderOutboxStatus.PENDING);

            outboxRepository.save(outboxEvent);

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize OrderCreatedEvent for outbox",
                    ex
            );
        }
    }

    @Transactional
    public void savePaymentRequestedEvent(PaymentRequestedEvent event) {

        try {
            OrderOutboxEvent outboxEvent = new OrderOutboxEvent();

            outboxEvent.setId(event.eventId());
            outboxEvent.setEventType("PAYMENT_REQUESTED");
            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(event.orderId());
            outboxEvent.setTopic("payment-requests");
            outboxEvent.setMessageKey(event.orderId().toString());
            outboxEvent.setPayload(
                    objectMapper.writeValueAsString(event)
            );
            outboxEvent.setStatus(OrderOutboxStatus.PENDING);

            outboxRepository.save(outboxEvent);

            log.info(
                    "PaymentRequestedEvent saved to outbox. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );

        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                    "Could not serialize PaymentRequestedEvent for outbox",
                    ex
            );
        }
    }

    @Transactional
    public void saveInventoryDeductionRequestedEvent(
            InventoryDeductionRequestedEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            OrderOutboxEvent outboxEvent = new OrderOutboxEvent();
            outboxEvent.setId(event.eventId());
            outboxEvent.setEventType("INVENTORY_DEDUCTION_REQUESTED");
            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(event.orderId());
            outboxEvent.setTopic("inventory-deduction-requests");
            outboxEvent.setMessageKey(event.orderId().toString());
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OrderOutboxStatus.PENDING);
            outboxEvent.setAttempts(0);

            outboxRepository.save(outboxEvent);

            log.info(
                    "InventoryDeductionRequestedEvent saved to outbox. eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize InventoryDeductionRequestedEvent",
                    exception
            );
        }
    }

    @Transactional
    public void saveInventoryCompensationRequestedEvent(
            InventoryCompensationRequestedEvent event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            OrderOutboxEvent outboxEvent = new OrderOutboxEvent();
            outboxEvent.setId(event.eventId());
            outboxEvent.setEventType("INVENTORY_COMPENSATION_REQUESTED");
            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(event.orderId());
            outboxEvent.setTopic("inventory-compensation-requests");
            outboxEvent.setMessageKey(event.orderId().toString());
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OrderOutboxStatus.PENDING);
            outboxEvent.setAttempts(0);

            outboxRepository.save(outboxEvent);

            log.info(
                    "InventoryCompensationRequestedEvent saved to outbox. "
                            + "eventId :: {}, orderId :: {}",
                    event.eventId(),
                    event.orderId()
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize InventoryCompensationRequestedEvent",
                    exception
            );
        }
    }

    @Transactional
    public void saveOrderConfirmedEvent(OrderStatusNotificationEvent event) {

        saveOrderStatusNotificationEvent(
                event,
                "ORDER_CONFIRMED"
        );
    }

    @Transactional
    public void saveOrderCancelledEvent(OrderStatusNotificationEvent event) {

        saveOrderStatusNotificationEvent(
                event,
                "ORDER_CANCELLED"
        );
    }

    private void saveOrderStatusNotificationEvent(OrderStatusNotificationEvent event, String eventType) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            OrderOutboxEvent outboxEvent = new OrderOutboxEvent();

            outboxEvent.setId(event.eventId());
            outboxEvent.setEventType(eventType);
            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(event.orderId());
            outboxEvent.setTopic(ORDER_STATUS_EVENTS_TOPIC);
            outboxEvent.setMessageKey(event.orderId().toString());
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(OrderOutboxStatus.PENDING);
            outboxEvent.setAttempts(0);

            outboxRepository.save(outboxEvent);

            log.info(
                    "{} saved to outbox. eventId :: {}, orderId :: {}",
                    eventType,
                    event.eventId(),
                    event.orderId()
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize " + eventType,
                    exception
            );
        }
    }
}
