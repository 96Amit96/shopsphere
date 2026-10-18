package com.shopsphere.orderservice.dto.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record InventoryCompensationResultEvent(

        UUID eventId,
        UUID compensationRequestEventId,
        Long orderId,
        Long userId,
        boolean successful,
        String failureReason,
        LocalDateTime occurredAt
) {
}
