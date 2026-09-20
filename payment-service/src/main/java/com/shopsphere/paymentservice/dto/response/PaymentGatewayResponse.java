package com.shopsphere.paymentservice.dto.response;

public record PaymentGatewayResponse(

        boolean successful,

        String transactionId,

        String message
) {
}
