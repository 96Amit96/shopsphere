package com.shopsphere.productservice.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record ProductSearchRequest(
        String search,

        Long categoryId,

        String brand,

        @DecimalMin(
                value = "0.0",
                message = "Minimum price cannot be negative"
        )
        BigDecimal minPrice,

        @DecimalMin(
                value = "0.0",
                message = "Maximum price cannot be negative"
        )
        BigDecimal maxPrice,

        @Min(value = 0,
        message = "Page must be greater than or equal to 0")
        Integer page,

        @Min(
                value = 1,
                message = "Page size must be at least 1"
        )
        @Max(
                value = 100,
                message = "Page size must not exceed 100"
        )
        Integer size,

        String sortBy,

        String direction
) {
}
