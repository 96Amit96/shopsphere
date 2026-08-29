package com.shopsphere.productservice.service.specification;

import com.shopsphere.productservice.entity.Product;
import com.shopsphere.productservice.enums.ProductStatus;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;

public final class ProductSpecification {

    private ProductSpecification(){}

    public static Specification<Product> hasStatus(
            ProductStatus status
    ) {
        return  ((root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("status"),
                        status
                )
                );
    }

    public static Specification<Product> search(
            String search
    ) {
        return (root, query, criteriaBuilder) -> {

            String pattern = "%" + search.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("name")
                            ),
                            pattern
                    ),
                    criteriaBuilder.like(
                            criteriaBuilder.lower(
                                    root.get("sku")
                            ),
                            pattern
                    )
            );
        };
    }

    public static Specification<Product> hasCategory(
            Long categoryId
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("category").get("id"),
                        categoryId
                );
    }

    public static Specification<Product> hasBrand(
            String brand
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        criteriaBuilder.lower(
                                root.get("brand")
                        ),
                        brand.toLowerCase()
                );
    }

    public static Specification<Product> priceGreaterThanOrEqualTo(
            BigDecimal minPrice
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        minPrice
                );
    }

    public static Specification<Product> priceLessThanOrEqualTo(
            BigDecimal maxPrice
    ) {
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        maxPrice
                );
    }
}
