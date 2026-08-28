package com.shopsphere.productservice.dto.request;

import com.shopsphere.productservice.enums.ProductStatus;
import jakarta.validation.constraints.NotNull;

public record ProductStatusUpdateRequest(
        @NotNull(message = "Product status is required")
        ProductStatus status
) {
}
