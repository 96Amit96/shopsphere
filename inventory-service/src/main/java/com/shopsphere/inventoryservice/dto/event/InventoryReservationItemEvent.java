package com.shopsphere.inventoryservice.dto.event;

public record InventoryReservationItemEvent(

        Long productId,
        Integer quantity
) {
}
