package com.shopsphere.inventoryservice.dto.response;

import com.shopsphere.inventoryservice.enums.ProductStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        BigDecimal price,
        Long categoryId,
        String categoryName,
        String brand,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
