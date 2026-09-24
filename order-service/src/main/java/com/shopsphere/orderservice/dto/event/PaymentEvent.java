package com.shopsphere.orderservice.dto.event;

import com.shopsphere.orderservice.enums.PaymentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentEvent(

        UUID eventId,
        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        String transactionId
) {
}
