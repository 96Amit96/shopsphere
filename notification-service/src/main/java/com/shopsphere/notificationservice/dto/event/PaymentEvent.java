package com.shopsphere.notificationservice.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEvent(
        UUID eventId,
        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        String paymentStatus,
        String transactionId
) {
}
