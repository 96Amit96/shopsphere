package com.shopsphere.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockDeductionRequest(

        @NotNull(message = "Deduction quantity is required")
        @Min(value = 1, message = "Deduction quantity must be at least 1")
        Integer quantity
) {
}
