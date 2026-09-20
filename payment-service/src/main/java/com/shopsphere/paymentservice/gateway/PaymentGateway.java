package com.shopsphere.paymentservice.gateway;

import com.shopsphere.paymentservice.dto.response.PaymentGatewayResponse;
import com.shopsphere.paymentservice.entity.Payment;

public interface PaymentGateway {

    PaymentGatewayResponse processPayment(Payment payment);
}
