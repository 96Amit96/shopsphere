package com.shopsphere.orderservice.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryCompensationRequestedEvent(

        UUID eventId,
        Long orderId,
        Long userId,
        List<InventoryCompensationItemEvent> items,
        String reason,
        LocalDateTime occurredAt
) {
}
