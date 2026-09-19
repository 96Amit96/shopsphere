package com.shopsphere.paymentservice.dto.response;

import com.shopsphere.paymentservice.enums.PaymentMethod;
import com.shopsphere.paymentservice.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentResponse(

        Long id,
        Long orderId,
        Long userId,
        BigDecimal amount,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String transactionId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
