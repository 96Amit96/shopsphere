package com.shopsphere.inventoryservice.dto.event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record InventoryDeductionRequestedEvent(
        UUID eventId,
        UUID paymentEventId,
        Long orderId,
        Long userId,
        List<InventoryDeductionItemEvent> items,
        LocalDateTime occurredAt
) {
}
