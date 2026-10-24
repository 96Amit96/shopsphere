package com.shopsphere.inventoryservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.inventoryservice.dto.event.InventoryCompensationResultEvent;
import com.shopsphere.inventoryservice.dto.event.InventoryDeductionResultEvent;
import com.shopsphere.inventoryservice.dto.event.InventorySagaEvent;
import com.shopsphere.inventoryservice.entity.InventoryOutboxEvent;
import com.shopsphere.inventoryservice.enums.InventoryOutboxStatus;
import com.shopsphere.inventoryservice.repository.InventoryOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryOutboxService {

    private final ObjectMapper objectMapper;
    private final InventoryOutboxEventRepository inventoryOutboxEventRepository;

    @Transactional
    public void saveInventorySagaEvent(InventorySagaEvent event) {
        saveEvent(
                event.eventId(),
                String.valueOf((event.eventType())),
                event.orderId(),
                "inventory-events",
                event
        );
    }

    @Transactional
    public void saveInventoryCompensationResultEvent(
            InventoryCompensationResultEvent event) {

        saveEvent(
                event.eventId(),
                "INVENTORY_COMPENSATION_RESULT",
                event.orderId(),
                "inventory-compensation-results",
                event
        );
    }

    @Transactional
    public void saveInventoryDeductionResultEvent(
            InventoryDeductionResultEvent event) {

        saveEvent(
                event.eventId(),
                "INVENTORY_DEDUCTION_RESULT",
                event.orderId(),
                "inventory-deduction-results",
                event
        );
    }

    private void saveEvent(
            UUID eventId,
            String eventType,
            Long orderId,
            String topic,
            Object event) {

        try {
            String payload = objectMapper.writeValueAsString(event);

            InventoryOutboxEvent outboxEvent = new InventoryOutboxEvent();
            outboxEvent.setId(eventId);
            outboxEvent.setEventType(eventType);
            outboxEvent.setAggregateType("ORDER");
            outboxEvent.setAggregateId(orderId);
            outboxEvent.setTopic(topic);
            outboxEvent.setMessageKey(orderId.toString());
            outboxEvent.setPayload(payload);
            outboxEvent.setStatus(InventoryOutboxStatus.PENDING);
            outboxEvent.setAttempts(0);

            inventoryOutboxEventRepository.save(outboxEvent);

            log.info(
                    "Inventory Outbox event saved. eventId :: {}, "
                            + "eventType :: {}, orderId :: {}, topic :: {}",
                    eventId,
                    eventType,
                    orderId,
                    topic
            );

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Failed to serialize inventory event. eventId :: " + eventId,
                    exception
            );
        }
    }
}
