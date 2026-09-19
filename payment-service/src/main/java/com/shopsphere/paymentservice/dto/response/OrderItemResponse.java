package com.shopsphere.paymentservice.dto.response;

import java.math.BigDecimal;

public record OrderItemResponse(

        Long id,
        Long productId,
        String productName,
        BigDecimal unitPrice,
        Integer quantity,
        BigDecimal totalPrice
) {
}
