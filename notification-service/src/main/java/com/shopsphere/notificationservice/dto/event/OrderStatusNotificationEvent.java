package com.shopsphere.notificationservice.dto.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record OrderStatusNotificationEvent(
        UUID eventId,
        Long orderId,
        Long userId,
        BigDecimal totalAmount,
        String orderStatus,
        String reason,
        List<OrderItemEvent> items
) {
}
