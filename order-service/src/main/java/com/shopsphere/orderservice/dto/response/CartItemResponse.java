package com.shopsphere.orderservice.dto.response;

import java.math.BigDecimal;

public record CartItemResponse(

        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice
) {
}
