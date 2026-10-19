package com.shopsphere.inventoryservice.dto.event;

public record InventoryDeductionItemEvent(
        Long productId,
        Integer quantity
) {
}
