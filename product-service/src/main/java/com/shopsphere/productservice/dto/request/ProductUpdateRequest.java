package com.shopsphere.productservice.dto.request;

import com.shopsphere.productservice.enums.ProductStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ProductUpdateRequest(
        @NotBlank(message = "Product name is required")
        @Size(max = 200, message = "Product name must not exceed 200 characters")
        String name,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        @NotNull(message = "Price is required")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price,

        @NotNull(message = "Category ID is required")
        Long categoryId,

        @NotBlank(message = "Brand is required")
        @Size(max = 100, message = "Brand must not exceed 100 characters")
        String brand,

        @NotNull(message = "Product status is required")
        ProductStatus status
) {
}
