package com.shopsphere.orderservice.dto.event;

public record InventoryReservationItemEvent(

        Long productId,
        Integer quantity
) {
}
