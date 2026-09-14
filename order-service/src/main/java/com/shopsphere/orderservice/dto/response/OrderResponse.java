package com.shopsphere.orderservice.dto.response;

import com.shopsphere.orderservice.enums.OrderStatus;
import com.shopsphere.orderservice.enums.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(

        Long id,
        Long userId,
        OrderStatus orderStatus,
        PaymentStatus paymentStatus,
        BigDecimal totalAmount,
        List<OrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
