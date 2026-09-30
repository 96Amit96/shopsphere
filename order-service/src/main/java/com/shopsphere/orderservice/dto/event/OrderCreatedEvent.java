package com.shopsphere.orderservice.dto.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderCreatedEvent(

        UUID eventId,
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        List<OrderItemEvent> items
) {
}
