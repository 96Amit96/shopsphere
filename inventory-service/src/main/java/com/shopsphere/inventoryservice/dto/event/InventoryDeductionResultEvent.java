package com.shopsphere.inventoryservice.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryDeductionResultEvent(
        UUID eventId,
        UUID deductionRequestEventId,
        Long orderId,
        Long userId,
        boolean successful,
        String failureReason,
        LocalDateTime occurredAt
) {
}
