package com.shopsphere.inventoryservice.dto.response;

import java.time.LocalDateTime;

public record InventoryResponse(
        Long id,
        Long productId,
        Integer quantity,
        Integer reservedQuantity,
        Integer availableQuantity,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
