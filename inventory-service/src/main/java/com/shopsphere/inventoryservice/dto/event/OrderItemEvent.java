package com.shopsphere.inventoryservice.dto.event;

import java.math.BigDecimal;

public record OrderItemEvent(

        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity
) {
}
