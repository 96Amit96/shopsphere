package com.shopsphere.orderservice.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateOrderRequest(
        @NotBlank(message = "Payment method is required")
        String paymentMethod
) {
}
