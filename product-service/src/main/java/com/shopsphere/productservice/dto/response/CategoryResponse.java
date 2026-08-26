package com.shopsphere.productservice.dto.response;

import java.time.LocalDateTime;

public record CategoryResponse(
        Long id,
        String name,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
