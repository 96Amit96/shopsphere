package com.shopsphere.paymentservice.dto.response;

public record RazorpayOrderResponse(
        Long paymentId,
        Long orderId,
        String razorpayOrderId,
        String razorpayKeyId,
        Long amount,
        String currency
) {
}
