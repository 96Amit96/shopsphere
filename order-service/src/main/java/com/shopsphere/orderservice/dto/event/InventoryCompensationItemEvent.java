package com.shopsphere.orderservice.dto.event;

public record InventoryCompensationItemEvent(

        Long productId,
        Integer quantity
) {
}
