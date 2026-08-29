package com.shopsphere.productservice.config;

import java.util.Set;

public final class ProductSortConfig {
    private ProductSortConfig(){}

    public static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "name",
            "price",
            "brand",
            "createdAt",
            "updatedAt"
    );
}
