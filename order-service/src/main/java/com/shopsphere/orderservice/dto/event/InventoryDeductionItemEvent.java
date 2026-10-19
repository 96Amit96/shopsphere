package com.shopsphere.orderservice.dto.event;

public record InventoryDeductionItemEvent(
        Long productId,
        Integer quantity
) {
}
