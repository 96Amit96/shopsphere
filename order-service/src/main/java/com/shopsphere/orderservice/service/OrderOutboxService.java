package com.shopsphere.orderservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.orderservice.dto.event.OrderCreatedEvent;
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
}
