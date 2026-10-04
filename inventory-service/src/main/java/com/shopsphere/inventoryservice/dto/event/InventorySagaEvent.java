package com.shopsphere.inventoryservice.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventorySagaEvent(

        UUID eventId,

        UUID orderEventId,

        Long orderId,

        Long userId,

        InventoryEventType eventType,

        List<InventoryReservationItemEvent> items,

        Long failedProductId,

        String failureReason,

        LocalDateTime occurredAt
) {
}
