package com.shopsphere.inventoryservice.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record StockReservationRequest(
        @NotNull(message = "Reservation quantity is required")
        @Min(value = 1, message = "Reservation quantity must be at least 1")
        Integer quantity
) {
}
