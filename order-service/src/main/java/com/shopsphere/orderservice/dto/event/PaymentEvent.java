package com.shopsphere.orderservice.dto.event;

import com.shopsphere.orderservice.enums.PaymentStatus;

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
