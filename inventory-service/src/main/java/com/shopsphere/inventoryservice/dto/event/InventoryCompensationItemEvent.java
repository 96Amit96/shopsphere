package com.shopsphere.inventoryservice.dto.event;

public record InventoryCompensationItemEvent(

        Long productId,
        Integer quantity
) {
}
