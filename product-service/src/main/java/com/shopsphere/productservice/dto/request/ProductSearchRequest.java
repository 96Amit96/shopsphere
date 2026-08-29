package com.shopsphere.productservice.dto.request;

import java.math.BigDecimal;

public record ProductSearchRequest(
        String search,

        Long categoryId,

        String brand,

        BigDecimal minPrice,

        BigDecimal maxPrice,

        Integer page,

        Integer size,

        String sortBy,

        String direction
) {
}
