package com.shopsphere.paymentservice.dto.event;

import com.shopsphere.paymentservice.enums.PaymentStatus;

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
