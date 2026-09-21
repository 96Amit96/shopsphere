package com.shopsphere.paymentservice.dto.event;

import com.shopsphere.paymentservice.enums.PaymentStatus;

import java.math.BigDecimal;

public record PaymentEvent(

        Long paymentId,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentStatus paymentStatus,
        String transactionId
) {
}
