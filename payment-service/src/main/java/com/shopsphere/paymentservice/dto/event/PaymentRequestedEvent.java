package com.shopsphere.paymentservice.dto.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentRequestedEvent(

        UUID eventId,
        UUID inventoryEventId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String paymentMethod,
        LocalDateTime occurredAt
) {
}
