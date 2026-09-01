package com.shopsphere.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record InventoryUpdateRequest(
        @NotNull(message = "Quantity is required")
        @Min(value = 0, message = "Quantity cannot be negative")
        Integer quantity
) {
}
